import yfinance as yf
from datetime import datetime

def print_option_chain(ticker, expiration_date):
    """
    Prints the option chain for a given stock ticker and expiration date.
    
    Parameters:
    ticker (str): Stock ticker symbol (e.g., 'AAPL', 'MSFT')
    expiration_date (str): Expiration date in 'YYYY-MM-DD' format
    """
    try:
        # Create ticker object
        stock = yf.Ticker(ticker)
        
        # Get option chain for the specified expiration date
        option_chain = stock.option_chain(expiration_date)
        
        # Print calls
        print(f"\n{'='*80}")
        print(f"Option Chain for {ticker.upper()} - Expiration: {expiration_date}")
        print(f"{'='*80}\n")
        
        print("CALL OPTIONS:")
        print("-" * 80)
        print(option_chain.calls.to_string())
        
        print(f"\n{'='*80}\n")
        
        # # Print puts
        # print("PUT OPTIONS:")
        # print("-" * 80)
        # print(option_chain.puts.to_string())
        
    except Exception as e:
        print(f"Error retrieving option chain: {e}")
        print(f"\nAvailable expiration dates for {ticker.upper()}:")
        try:
            stock = yf.Ticker(ticker)
            print(stock.options)
        except:
            print("Could not retrieve available expiration dates.")

# Example usage:
if __name__ == "__main__":
    print_option_chain("SPY","2026-04-02")