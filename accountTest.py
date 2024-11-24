from alpaca.trading.client import TradingClient



trading_key = ""

trading_secret = ""

trading_client = TradingClient(trading_key, trading_secret)
if "PA3OJSRWBIBI" == trading_client.get_account().account_number:
    print("Athmalysis Brokerage Account: Paper")

print("Account Number: "+trading_client.get_account().account_number)
print("Cash: "+trading_client.get_account().cash+" $"+trading_client.get_account().currency)
