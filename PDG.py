import numpy as np
import matplotlib.pyplot as plt
from scipy.interpolate import UnivariateSpline
from scipy.ndimage import gaussian_filter1d
import yfinance as yf
from datetime import datetime

def plot_risk_neutral_density(ticker, expiration_date, r=0.045, 
                               price_range_pct=None, min_oi=30, smoothing_s=0.1):
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
            print("Error: Expiration date is in the past")
            return
        
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
        

        print("price_range_pct:")
        print(price_range_pct)
        # Only use options within specified range of current price
        lower_bound = current_price * (1 - price_range_pct)
        upper_bound = current_price * (1 + price_range_pct)
        calls = calls[(calls['strike'] >= lower_bound) & (calls['strike'] <= upper_bound)]
        
        calls = calls.sort_values('strike')
        
        if len(calls) < 10:
            print(f"Warning: Only {len(calls)} liquid options found. Results may be unreliable.")
            if len(calls) < 5:
                print("Error: Not enough data points for reliable density estimation")
                return
        
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
        
        return K_grid, density_smooth, mean_price, std_price
        
    except Exception as e:
        import traceback
        print(f"Error: {e}")
        traceback.print_exc()


# Example usage
if __name__ == "__main__":
    plot_risk_neutral_density("SPY", "2026-02-13")
    plot_risk_neutral_density("SPY", "2026-02-20")
    plot_risk_neutral_density("SPY", "2026-04-17")
    plot_risk_neutral_density("SPY", "2026-06-30")
    plot_risk_neutral_density("SPY", "2027-01-15")


    # SPY Expirations:
    # 1 Week 2026-01-30
    # 2 Weeks 2026-02-06
    # 1 Month 2026-02-20
    # 3 Months 2026-04-17
    # 6 Months 2026-06-30
    # 1 Year 2027-01-15