trading_key = ""

trading_secret = ""


from alpaca.trading.client import TradingClient
from alpaca.trading.requests import (
    LimitOrderRequest,
    StopLimitOrderRequest,
    TakeProfitRequest,
    StopLossRequest,
    OrderRequest
)
from alpaca.trading.enums import OrderSide, TimeInForce, OrderType, OrderClass
import sys
import time
import random

# Inputs from command line
stock = sys.argv[1]
quantity = int(sys.argv[2])  # Ensure quantity is an integer
buy_limit = float(sys.argv[3])  # Limit price for buying the stock
stop = float(sys.argv[4])  # Stop-loss trigger price
sell_point = float(sys.argv[5])  # Take-profit (sell) limit price

print("Buying Stock: " + stock)
print("Quantity: " + str(quantity))
print("Buy Limit Price: " + str(buy_limit))
print("Stop Loss: " + str(stop))
print("Sell Point: " + str(sell_point))

# Trading API keys


# Initialize the trading client
trading_client = TradingClient(trading_key, trading_secret)


type = "limit"
limit_price = buy_limit
order_class = "bracket"
take_profit = {"limit_price": sell_point}    # 10% gain sets this value to $275
stop_loss = {"stop_price": stop}       # 5% loss sets this value to $237.50
client_order_id=f"gcos_{random.randrange(100000000)}"


trading_client.submit_order(stock,
                    qty=quantity, 
                    type=type,
                    limit_price=limit_price, 
                    order_class=order_class,
                    take_profit=take_profit,
                    stop_loss=stop_loss,
                    client_order_id=client_order_id)