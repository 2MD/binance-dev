Telegram bot, which polls Binance Web Socket and check percent
between diff candle point 3 (open price) and point 1 (Upper price) for input period.
Also, bot monitorings all pair of coins with priority in USB. If bot finds a new pair of coins, his
will notify about it.

Telegram bot accepts the following parameters as input:
1) period (for candle 1m, 15m, 1h,.. etc)
2) percent (for trigger, when we need to notify about diff)

**Telegram bot has the next commands**:
 - /start (Create settings for bot)
 - /reset (Reset settings for bot)
 - /all   (All coins which bot monitorings)
 - /help  (It's main command menu with description)

**Algorithm**

We have a multithreading application with cash state.
We have several variables with global states:

| Params | Mean                                        | Calculate                                                                                                                                                                                                                                                                                                                    |
|--------|---------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|CASH_ACTUAL_LIST_OF_COINS | it's list of all coins for monitoring | We get all coins from **BUSD** + all coins from **USDT**, that aren't in BUSD + all coins from **BTC**, that aren't in BUSD and USDT + all coins from **BNB**, that aren't in BUSD and USDT and BTC. This list we save in saveState.txt file.                                                                                |
|CASH_SETTINGS_CANDLE_MONITORING | it's settings, that were created across bot for monitoring. Such as interval (period), percent.| Input data from user across bot. This list we save in saveState.txt file.                                                                                                                                                                                                                                                                                            |
|EVENTS| It's list of events, that are torn off satisfied the given criteria from users settings| We need to send just one event for each period and pair of coins for some chart in one minute. We save in this variable all events, that we've already sent. All new events, that are torn off satisfied of users settings we check with this list. We remove all events with datetime more or eq 1 minute ago every minute. 

**Http**

We get all pair of coins across **Binance API**:
https://api.binance.com/api/v3/exchangeInfo?permissions=SPOT

Every 10 minutes. All input pair of coins we check with our CASH_ACTUAL_LIST_OF_COINS. If we find a new pair of coin, we will send this information across bot and update cash in saveState.txt file.

We subscribe foreach pair of coin + period on Binance socket wss://stream.binance.com:9443/ws

Example: 

params: {{some_pair_of_coin1}@kline_{period}, {some_pair_of_coin2}@kline_{period2}, .. etc}



