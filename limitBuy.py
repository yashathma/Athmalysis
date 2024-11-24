# from alpaca.trading.client import TradingClient
# from alpaca.trading.requests import LimitOrderRequest, TakeProfitRequest, StopLossRequest
# from alpaca.trading.enums import OrderSide, TimeInForce, QueryOrderStatus
# import time
# import sys

# # Inputs from command line
# stock = sys.argv[1]
# quantity = sys.argv[2]
# lim = sys.argv[3]
# stop_loss = sys.argv[4]
# sell_point = sys.argv[5]

# print("Buying Stock: " + stock)
# print("Quantity: " + quantity)
# print("Limit Price: " + lim)
# print("Stop Loss: " + stop_loss)
# print("Sell Point: " + sell_point)

# # Trading API keys
# trading_key = ""

# trading_secret = ""

# # Initialize the trading client
# trading_client = TradingClient(trading_key, trading_secret)

# # Step 1: Place a limit buy order
# market_order_buy = LimitOrderRequest(
#     symbol=stock,
#     qty=quantity,
#     side=OrderSide.BUY,
#     time_in_force=TimeInForce.DAY,
#     limit_price=lim
# )

# # Submit the buy limit order
# buy_order = trading_client.submit_order(market_order_buy)

# # Check if the buy order was submitted successfully
# order_id = buy_order.id
# #print(f"Buy Order Status: {buy_order.status}")

# # Wait for max_wait_time seconds to check if the order is filled
# time_waited = 0
# check_interval = 1  # Check frequency
# max_wait_time = 10  # Wait time is max_wait_time seconds

# order_status = trading_client.get_order_by_id(order_id).status

# while time_waited < max_wait_time:
#     # Get the latest status of the order
#     order_status = trading_client.get_order_by_id(order_id).status

#     if order_status == "filled":
#         print("Order filled.")
#         break
#     elif order_status == "canceled":
#         print("Order was canceled by the broker or user.")
#         sys.exit(0)
    
#     # Wait for the next ch4eck
#     time.sleep(check_interval)
#     time_waited += check_interval

# # If the order is not filled after the max wait time, cancel it
# if order_status != "filled":
#     print("Order not filled in time. Canceling the order...")
#     trading_client.cancel_order_by_id(order_id)
#     sys.exit("Order canceled because it wasn't filled within "+str(max_wait_time)+" seconds.")

# # # Step 2: Place a stop-loss order only after the buy order has been filled
# # stop_loss_order = StopOrderRequest(
# #     symbol=stock,
# #     qty=quantity,
# #     side=OrderSide.SELL,
# #     time_in_force=TimeInForce.GTC,  # Good 'Til Canceled for stop-loss
# #     stop_price=stop_loss            # Stop price at which to trigger a sell
# # )

# # # Submit the stop-loss order
# # stop_loss_response = trading_client.submit_order(stop_loss_order)

# # print(f"Stop Loss Order Status: {stop_loss_response.status}")




# # # Step 3: Place a limit sell order at the specified sell and stop loss point
# # take_profit_order = TakeProfitRequest(
# #     limit_price=sell_point  # Profit-taking limit price
# # )

# # stop_loss_order = StopLossRequest(
# #     stop_price=stop_loss,   # Stop-loss trigger price
# #     limit_price=float(stop_loss) - 0.5  # Limit price after the stop is triggered
# # )


# # oco_order = LimitOrderRequest(
# #     symbol=stock,
# #     qty=quantity,
# #     side=OrderSide.SELL,
# #     time_in_force=TimeInForce.GTC,  # Good 'Til Canceled
# #     order_class="oco",  # One-Cancels-Other
# #     take_profit=take_profit_order,
# #     stop_loss=stop_loss_order
# # )

# # # Submit the OCO order
# # oco_order = trading_client.submit_order(oco_order)
# # print(f"OCO Order Status: {oco_order.status}")



# # Step 2: Place an OCO (One-Cancels-Other) order after the buy order has been filled

# # Create Take Profit and Stop Loss requests for the OCO order
# take_profit_order = TakeProfitRequest(
#     limit_price=sell_point  # Profit-taking limit price
# )

# stop_loss_order = StopLossRequest(
#     stop_price=stop_loss,   # Stop-loss trigger price
#     limit_price=float(stop_loss) - 0.5  # Limit price after the stop is triggered
# )

# # Submit the OCO order
# oco_order = MarketOrderRequest(
#     symbol=stock,
#     qty=quantity,
#     side=OrderSide.SELL,
#     time_in_force=TimeInForce.GTC,  # Good 'Til Canceled
#     order_class="oco",  # One-Cancels-Other
#     take_profit=take_profit_order,
#     stop_loss=stop_loss_order
# )

# oco_order_response = trading_client.submit_order(oco_order)
# print(f"OCO Order Status: {oco_order_response.status}")

# Step 2: Place an OCO (One-Cancels-Other) order after the buy order has been filled

# Create Take Profit and Stop Loss requests for the OCO order
# take_profit_order = TakeProfitRequest(
#     limit_price=sell_point  # Profit-taking limit price
# )

# stop_loss_order = StopLossRequest(
#     stop_price=stop_loss,   # Stop-loss trigger price
#     limit_price=float(stop_loss) - 0.5  # Limit price after the stop is triggered
# )

# # The main limit price needs to be present in the OCO order
# oco_order = LimitOrderRequest(
#     symbol=stock,
#     qty=quantity,
#     side=OrderSide.SELL,
#     time_in_force=TimeInForce.GTC,  # Good 'Til Canceled
#     order_class="oco",  # One-Cancels-Other
#     limit_price=sell_point,  # Required limit price for the OCO order
#     take_profit=take_profit_order,
#     stop_loss=stop_loss_order
# )

# oco_order_response = trading_client.submit_order(oco_order)
# print(f"OCO Order Status: {oco_order_response.status}")


# from alpaca.trading.client import TradingClient
# from alpaca.trading.requests import LimitOrderRequest, TakeProfitRequest, StopLossRequest
# from alpaca.trading.enums import OrderSide, TimeInForce
# import sys
# import time

# # Inputs from command line
# stock = sys.argv[1]
# quantity = sys.argv[2]
# buy_limit = sys.argv[3]  # Limit price for buying the stock
# stop_loss = sys.argv[4]  # Stop-loss trigger price
# sell_point = sys.argv[5]  # Take-profit (sell) limit price

# print("Buying Stock: " + stock)
# print("Quantity: " + quantity)
# print("Buy Limit Price: " + buy_limit)
# print("Stop Loss: " + stop_loss)
# print("Sell Point: " + sell_point)

# # Trading API keys
trading_key = ""

trading_secret = ""

# # Initialize the trading client
# trading_client = TradingClient(trading_key, trading_secret)

# # Step 1: Place a limit buy order
# limit_buy_order = LimitOrderRequest(
#     symbol=stock,
#     qty=quantity,
#     side=OrderSide.BUY,
#     time_in_force=TimeInForce.DAY,
#     limit_price=buy_limit  # Your limit price for buying
# )

# # Submit the buy limit order
# buy_order = trading_client.submit_order(limit_buy_order)

# # Check if the buy order was submitted successfully
# order_id = buy_order.id
# print(f"Buy Order Status: {buy_order.status}")

# # Wait for the buy order to be filled
# time_waited = 0
# check_interval = 1  # Check frequency in seconds
# max_wait_time = 10  # Maximum wait time in seconds

# order_status = trading_client.get_order_by_id(order_id).status

# while time_waited < max_wait_time:
#     # Get the latest status of the order
#     order_status = trading_client.get_order_by_id(order_id).status

#     if order_status == "filled":
#         print("Order filled.")
#         break
#     elif order_status == "canceled":
#         print("Order was canceled by the broker or user.")
#         sys.exit(0)
    
#     # Wait for the next check
#     time.sleep(check_interval)
#     time_waited += check_interval

# # If the order is not filled after the max wait time, cancel it
# if order_status != "filled":
#     print("Order not filled in time. Canceling the order...")
#     trading_client.cancel_order_by_id(order_id)
#     sys.exit(f"Order canceled because it wasn't filled within {max_wait_time} seconds.")



# #Add logic that places an oco order, where I want to sell if the price either hits the sell_point or the stop_loss




from alpaca.trading.client import TradingClient
from alpaca.trading.requests import (
    LimitOrderRequest,
    TakeProfitRequest,
    StopLossRequest,
)
from alpaca.trading.enums import OrderSide, TimeInForce, OrderType, OrderClass
import sys
import time

# Inputs from command line
stock = sys.argv[1]
quantity = int(sys.argv[2])  # Ensure quantity is an integer
buy_limit = float(sys.argv[3])  # Limit price for buying the stock
stop_loss = float(sys.argv[4])  # Stop-loss trigger price
sell_point = float(sys.argv[5])  # Take-profit (sell) limit price

print("Buying Stock: " + stock)
print("Quantity: " + str(quantity))
print("Buy Limit Price: " + str(buy_limit))
print("Stop Loss: " + str(stop_loss))
print("Sell Point: " + str(sell_point))

# Trading API keys

# Initialize the trading client
trading_client = TradingClient(trading_key, trading_secret, paper=True)

# Step 1: Place a limit buy order
limit_buy_order = LimitOrderRequest(
    symbol=stock,
    qty=quantity,
    side=OrderSide.BUY,
    time_in_force=TimeInForce.DAY,
    limit_price=buy_limit,  # Your limit price for buying
)

# Submit the buy limit order
buy_order = trading_client.submit_order(limit_buy_order)

# Check if the buy order was submitted successfully
order_id = buy_order.id
print(f"Buy Order Status: {buy_order.status}")

# Wait for the buy order to be filled
time_waited = 0
check_interval = 1  # Check frequency in seconds
max_wait_time = 10  # Maximum wait time in seconds

while time_waited < max_wait_time:
    # Get the latest status of the order
    order_status = trading_client.get_order_by_id(order_id).status

    if order_status == "filled":
        print("Buy order filled.")
        break
    elif order_status == "canceled":
        print("Order was canceled by the broker or user.")
        sys.exit(0)

    # Wait for the next check
    time.sleep(check_interval)
    time_waited += check_interval

# If the order is not filled after the max wait time, cancel it
if order_status != "filled":
    print("Order not filled in time. Canceling the order...")
    trading_client.cancel_order_by_id(order_id)
    sys.exit(
        f"Order canceled because it wasn't filled within {max_wait_time} seconds."
    )

# Step 2: Place an OCO sell order with take-profit and stop-loss
# Define the take-profit and stop-loss requests
take_profit = TakeProfitRequest(limit_price=sell_point)
stop_loss_request = StopLossRequest(stop_price=stop_loss)

# Create the OCO sell order request
oco_sell_order = LimitOrderRequest(
    symbol=stock,
    qty=quantity,
    side=OrderSide.SELL,
    type=OrderType.LIMIT,  # The main order type
    time_in_force=TimeInForce.GTC,
    limit_price=sell_point,  # Set the limit price for the take-profit leg
    order_class=OrderClass.OCO,
    take_profit=take_profit,
    stop_loss=stop_loss_request,
)

# Submit the OCO sell order
sell_order = trading_client.submit_order(oco_sell_order)

# Print the sell order status
print(f"OCO Sell Order Status: {sell_order.status}")