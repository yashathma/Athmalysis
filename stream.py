from alpaca.data.live import StockDataStream

import sys

trading_key = ""

trading_secret = ""

stream = StockDataStream(trading_key, trading_secret)

stock = sys.argv[1]

print("Streaming the following Stock: "+stock)



async def handel_trade(data):
    data = str(data)

    for part in data.split():
        if part.startswith("price="):
            # Remove 'price=' and print just the price value
            price = part.split('=')[1]
            print("USD$ "+price)


stream.subscribe_trades(handel_trade, stock)

stream.run()