# crypto-trader
Program listens for feeds from various market influencers. After receiving singal it makes buy order and 5% higher (configurable) sell order on Bittrex.
Price on Bittrex is monitored, if sell order is not filled and price drops by 20%, stop loss is executed.
