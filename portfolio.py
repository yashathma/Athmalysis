from alpaca.trading.client import TradingClient
from alpaca.trading.requests import GetOrdersRequest
from alpaca.trading.enums import OrderSide, QueryOrderStatus

trading_key = ""

trading_secret = ""

trading_client = TradingClient(trading_key, trading_secret)

# Get all current positions
positions = trading_client.get_all_positions()

# Variables to track total portfolio performance
total_net_profit = 0
total_initial_value = 0

# Fetch open stop-loss orders for all positions
open_orders = trading_client.get_orders(
    GetOrdersRequest(status=QueryOrderStatus.OPEN, side=OrderSide.SELL)
)

# Create a dictionary to map stop-loss prices for each symbol
stop_losses = {}
for order in open_orders:
    if order.stop_price is not None:
        stop_losses[order.symbol] = float(order.stop_price)



# Output formatting
print("-" * 30)
for position in positions:
    symbol = position.symbol
    qty = float(position.qty)
    avg_entry_price = float(position.avg_entry_price)
    current_price = float(position.current_price)

    # Calculate net profit and percentage gain/loss
    net_profit = (current_price - avg_entry_price) * qty
    total_net_profit += net_profit

    initial_value = avg_entry_price * qty
    total_initial_value += initial_value

    percentage_gain = (net_profit / initial_value) * 100

    # Fetch stop-loss for the current position (if available)
    stop_loss = stop_losses.get(symbol, "No stop loss set")

    # Print position details
    print(f"Symbol: {symbol}")
    print(f"Quantity: {qty}")
    print(f"Avg Entry Price: ${avg_entry_price:.2f}")
    print(f"Current Price: ${current_price:.2f}")
    print(f"Net Profit: ${net_profit:.2f}")
    print(f"Percentage Gain/Loss: {percentage_gain:.2f}%")
    print(f"Stop Loss: {stop_loss}")
    print("-" * 30)

# Calculate total portfolio gain/loss
if total_initial_value > 0:
    total_percentage_gain = (total_net_profit / total_initial_value) * 100
    print(f"Total Portfolio Net Profit: ${total_net_profit:.2f}")
    print(f"Total Portfolio Percentage Gain/Loss: {total_percentage_gain:.2f}%")
else:
    print("No open positions in the portfolio.")

print("-" * 30)