import numpy as np
import matplotlib.pyplot as plt
from scipy.interpolate import UnivariateSpline
from scipy.ndimage import gaussian_filter1d
import yfinance as yf
from datetime import datetime, timedelta
from matplotlib.colors import LinearSegmentedColormap

def plot_risk_neutral_density(ticker, expiration_date, r=0.045,
                               price_range_pct=None, min_oi=30, smoothing_s=0.1,
                               plot=True):
    """
    Plots the risk-neutral probability density using Breeden-Litzenberger formula.

    Parameters:
    -----------
    ticker : str
        Stock ticker symbol
    expiration_date : str
        Expiration date in 'YYYY-MM-DD' format
    r : float
        Risk-free rate (annualized), default 0.045
    price_range_pct : float
        Percentage range around current price to use (default 8% = ±8%)
    min_oi : int
        Minimum open interest to include an option (default 30)
    smoothing_s : float
        Spline smoothing parameter (default 0.1)
    plot : bool
        If True (default), display plots and print stats. If False, only return data.
    """
    try:
        # Get option chain
        stock = yf.Ticker(ticker)
        option_chain = stock.option_chain(expiration_date)
        calls = option_chain.calls.copy()
        
        # Get current stock price
        current_price = stock.history(period="1d")['Close'].iloc[-1]
        
        # Calculate time to expiration in years
        exp_date = datetime.strptime(expiration_date, '%Y-%m-%d')
        today = datetime.now()
        T = (exp_date - today).days / 365.25

        
        if T <= 0:
            if plot:
                print("Error: Expiration date is in the past")
            return

        if plot:
            print(f"Ticker: {ticker.upper()}")
            print(f"Current Price: ${current_price:.2f}")
            print(f"Expiration: {expiration_date}")
            print(f"Time to Expiration: {T:.4f} years ({(exp_date - today).days} days)")
        
        # Remove options with no open interest or invalid prices
        calls = calls[calls['openInterest'] >= min_oi].copy()
        calls['mid_price'] = (calls['bid'] + calls['ask']) / 2
        calls = calls[calls['mid_price'] > 0]
        calls = calls[calls['bid'] > 0]

        # Get ATM implied volatility to estimate expected move
        atm_mask = (calls['strike'] >= current_price * 0.98) & (calls['strike'] <= current_price * 1.02)
        atm_calls = calls[atm_mask]
        if len(atm_calls) > 0:
            atm_iv = atm_calls['impliedVolatility'].median()
        else:
            # Fallback: use median IV of all options
            atm_iv = calls['impliedVolatility'].median()
        
        # Expected 1-sigma move = IV * sqrt(T) * price
        expected_move_pct = atm_iv * np.sqrt(T)
        
        # Set price_range to capture ~3 sigma of expected distribution
        # This ensures we get the full bell curve
        if price_range_pct is None:
            # Use 3x the expected move, with minimum of 5% and max of 50%
            price_range_pct = np.clip(3 * expected_move_pct, 0.05, 0.50)
        

        if plot:
            print("price_range_pct:")
            print(price_range_pct)
        # Only use options within specified range of current price
        lower_bound = current_price * (1 - price_range_pct)
        upper_bound = current_price * (1 + price_range_pct)
        calls = calls[(calls['strike'] >= lower_bound) & (calls['strike'] <= upper_bound)]
        
        calls = calls.sort_values('strike')
        
        if len(calls) < 10:
            if plot:
                print(f"Warning: Only {len(calls)} liquid options found. Results may be unreliable.")
            if len(calls) < 5:
                if plot:
                    print("Error: Not enough data points for reliable density estimation")
                return

        if plot:
            print(f"Using {len(calls)} options in range ${lower_bound:.0f}-${upper_bound:.0f}")
        
        strikes = calls['strike'].values
        prices = calls['mid_price'].values
        
        # smoothness before taking second derivative
        spline = UnivariateSpline(strikes, prices, s=smoothing_s)
        
        # Create uniform strike grid
        dK = 0.5  # $0.50 spacing for fine resolution
        K_grid = np.arange(strikes.min() + dK, strikes.max() - dK, dK)
        C_smooth = spline(K_grid)
        
        # Compute second derivative on smooth curve
        # Using central difference: f''(x) ≈ [f(x+h) - 2f(x) + f(x-h)] / h²
        second_deriv = np.gradient(np.gradient(C_smooth, dK), dK)
        
        # Breeden-Litzenberger formula
        density = np.exp(r * T) * second_deriv
        
        # Remove negative densities (indicate arbitrage or data issues)
        density = np.maximum(density, 0)
        
        # Normalize to integrate to 1
        # Use np.trapz for compatibility with older NumPy versions
        total = np.trapz(density, K_grid)
        if total > 0:
            density = density / total
        
        # Apply Gaussian smoothing after computing density
        sigma = max(2, int(10 / dK))  # Roughly $5 smoothing window
        density_smooth = gaussian_filter1d(density, sigma=sigma/2)
        density_smooth = np.maximum(density_smooth, 0)
        
        # Re-normalize
        total = np.trapz(density_smooth, K_grid)
        if total > 0:
            density_smooth = density_smooth / total
        
        # stats
        mean_price = np.trapz(K_grid * density_smooth, K_grid)
        variance = np.trapz((K_grid - mean_price)**2 * density_smooth, K_grid)
        std_price = np.sqrt(variance)
        
        # Mode
        mode_idx = np.argmax(density_smooth)
        mode_price = K_grid[mode_idx]
        
        # Probability calculations
        prob_below = np.trapz(density_smooth[K_grid < current_price], 
                              K_grid[K_grid < current_price])
        prob_above = 1 - prob_below
        
        if plot:
            # ===== Plotting =====
            fig, axes = plt.subplots(2, 1, figsize=(14, 10))

            # Plot 1: Risk-neutral density
            PDF = axes[0]
            PDF.plot(K_grid, density_smooth, 'b-', linewidth=2)
            PDF.fill_between(K_grid, density_smooth, alpha=0.3)
            PDF.axvline(current_price, color='r', linestyle='--', linewidth=2,
                       label=f'Current: ${current_price:.2f}')
            PDF.axvline(mean_price, color='g', linestyle=':', linewidth=2,
                       label=f'RN Mean: ${mean_price:.2f}')
            PDF.set_xlabel('Stock Price at Expiration ($)', fontsize=11)
            PDF.set_ylabel('Probability Density', fontsize=11)
            PDF.set_title(f'{ticker.upper()} Risk-Neutral Probability Density\nExpiration: {expiration_date}')
            PDF.legend()
            PDF.grid(True, alpha=0.3)

            # Plot 2: Probability ranges
            VAR = axes[1]
            ranges = [1, 2, 3, 5]
            probs = []
            for pct in ranges:
                lower = current_price * (1 - pct/100)
                upper = current_price * (1 + pct/100)
                mask = (K_grid >= lower) & (K_grid <= upper)
                prob = np.trapz(density_smooth[mask], K_grid[mask]) if mask.any() else 0
                probs.append(prob)

            colors = plt.cm.Blues(np.linspace(0.4, 0.8, len(ranges)))
            bars = VAR.bar([f'±{p}%' for p in ranges], [100*p for p in probs],
                           color=colors, edgecolor='navy')
            VAR.set_ylabel('Probability (%)', fontsize=11)
            VAR.set_xlabel('Range around current price')
            VAR.set_title(f'Probability of ending within X% of ${current_price:.2f}')
            VAR.set_ylim(0, 100)
            for bar, prob in zip(bars, probs):
                VAR.text(bar.get_x() + bar.get_width()/2, bar.get_height() + 2,
                        f'{100*prob:.1f}%', ha='center', va='bottom', fontweight='bold')
            VAR.grid(True, alpha=0.3, axis='y')

            plt.tight_layout()
            plt.show()

            # Print statistics
            print(f"\n{'='*50}")
            print("RISK-NEUTRAL DISTRIBUTION STATISTICS")
            print(f"{'='*50}")
            print(f"Current Price:         ${current_price:.2f}")
            print(f"Risk-Neutral Mean:     ${mean_price:.2f}")
            print(f"Risk-Neutral Std Dev:  ${std_price:.2f}")
            print(f"Mode (peak):           ${mode_price:.2f}")
            print(f"\nExpected Change:       ${mean_price - current_price:.2f} "
                  f"({100*(mean_price/current_price - 1):.2f}%)")
            print(f"\nP(price < current):    {100*prob_below:.1f}%")
            print(f"P(price > current):    {100*prob_above:.1f}%")
            print(f"\nProbability within range:")
            for pct, prob in zip(ranges, probs):
                print(f"  ±{pct}%: {100*prob:.1f}%")

        return K_grid, density_smooth, current_price, mean_price, std_price
        
    except Exception as e:
        import traceback
        print(f"Error: {e}")
        traceback.print_exc()


def plot_options_heatmap(ticker, r=0.045, min_oi=30, smoothing_s=0.1):
    """
    Generates a heatmap showing where the options market predicts a stock's price
    will go over the next 12 weeks.

    Each row is a weekly expiration (next 12 Fridays). Color represents probability
    density: red = high probability, green = low probability.

    Parameters:
    -----------
    ticker : str
        Stock ticker symbol
    r : float
        Risk-free rate (annualized), default 0.045
    min_oi : int
        Minimum open interest to include an option (default 30)
    smoothing_s : float
        Spline smoothing parameter (default 0.1)
    """
    stock = yf.Ticker(ticker)
    available_expirations = stock.options  # list of 'YYYY-MM-DD' strings

    # Find next 12 Fridays
    today = datetime.now().date()
    # Find the first upcoming Friday (weekday 4 = Friday)
    days_until_friday = (4 - today.weekday()) % 7
    if days_until_friday == 0:
        # Today is Friday, use today as the first
        first_friday = today
    else:
        first_friday = today + timedelta(days=days_until_friday)

    target_fridays = [first_friday + timedelta(weeks=i) for i in range(12)]

    # Match each target Friday to the closest available expiration
    avail_dates = [datetime.strptime(d, '%Y-%m-%d').date() for d in available_expirations]

    matched_expirations = []
    for friday in target_fridays:
        closest = min(avail_dates, key=lambda d: abs((d - friday).days))
        # Only accept if within 3 days of the target Friday
        if abs((closest - friday).days) <= 3:
            matched_expirations.append(closest.strftime('%Y-%m-%d'))
        else:
            matched_expirations.append(None)

    # Compute densities for all 12 weeks (None for skipped weeks)
    # Each entry: (label, K_grid, density) or None for missing weeks
    all_weeks = []  # always 12 entries
    current_price = None
    has_data = False

    for i, exp_date in enumerate(matched_expirations):
        label = target_fridays[i].strftime('%Y-%m-%d')
        if exp_date is None:
            print(f"  Skipping week {i+1} ({label}) - no nearby expiration available")
            all_weeks.append(None)
            continue

        result = plot_risk_neutral_density(ticker, exp_date, r=r, min_oi=min_oi,
                                           smoothing_s=smoothing_s, plot=False)
        if result is None:
            print(f"  Skipping {exp_date} - insufficient data")
            all_weeks.append(None)
            continue

        K_grid, density_smooth, curr_price, _, _ = result
        if current_price is None:
            current_price = curr_price
        all_weeks.append((exp_date, K_grid, density_smooth))
        has_data = True

    if not has_data:
        print("Error: Could not compute density for any expiration date.")
        return

    valid = [(label, kg, d) for w in all_weeks if w is not None for label, kg, d in [w]]
    print(f"\nBuilding heatmap with {len(valid)} expiration dates ({12 - len(valid)} skipped)...")

    # Build a common price grid spanning all valid densities
    global_min = min(kg.min() for _, kg, _ in valid)
    global_max = max(kg.max() for _, kg, _ in valid)
    common_grid = np.arange(global_min, global_max, 0.5)

    # Build heatmap data for all 12 weeks (NaN for skipped weeks)
    heatmap_data = np.full((len(common_grid), 12), np.nan)
    date_labels = []

    for i, week in enumerate(all_weeks):
        label = target_fridays[i].strftime('%m/%d')
        if week is not None:
            exp_date, K_grid, density = week
            interp_density = np.interp(common_grid, K_grid, density, left=0, right=0)
            heatmap_data[:, i] = interp_density
            date_labels.append(exp_date)
        else:
            # Leave as NaN — will render as grey
            date_labels.append(f"{label}\n(N/A)")

    # Custom green-to-red colormap (green = low prob, red = high prob)
    cmap = LinearSegmentedColormap.from_list('green_red',
        ['#1a9641', '#a6d96a', '#ffffbf', '#fdae61', '#d7191c'])
    cmap.set_bad(color='#d9d9d9')  # grey for NaN (missing weeks)

    fig, ax = plt.subplots(figsize=(16, 8))

    im = ax.pcolormesh(np.arange(12), common_grid, heatmap_data,
                       cmap=cmap, shading='nearest')

    # X-axis: date labels
    ax.set_xticks(np.arange(12))
    ax.set_xticklabels(date_labels, fontsize=9, rotation=45, ha='right')
    ax.set_xlabel('Expiration Date', fontsize=12)

    # Y-axis: price
    ax.set_ylabel('Stock Price ($)', fontsize=12)

    # Current price horizontal line
    ax.axhline(current_price, color='white', linestyle='--', linewidth=2,
               label=f'Current: ${current_price:.2f}')
    ax.legend(loc='upper right', fontsize=10)

    ax.set_title(f'{ticker.upper()} Options-Implied Price Heatmap (Next 12 Weeks)\n'
                 f'Red = High Probability | Green = Low Probability | Grey = No Data',
                 fontsize=13)

    cbar = fig.colorbar(im, ax=ax, pad=0.02)
    cbar.set_label('Probability Density', fontsize=11)

    plt.tight_layout()
    plt.show()


# Example usage
if __name__ == "__main__":

    #plot_risk_neutral_density()
    plot_options_heatmap("SPY")


