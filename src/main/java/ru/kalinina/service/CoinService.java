package ru.kalinina.service;

import org.apache.commons.collections4.CollectionUtils;
import ru.kalinina.model.input.InputCoin;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CoinService {
    private List<InputCoin> inputCoins;

    private Set<String> transformInputCoin(Set<InputCoin> coins) {
        return coins.stream().map(InputCoin::getBaseAsset).collect(Collectors.toSet());
    }

    public Set<String> findUniqCoins() {
        Set<InputCoin> busdCoins = new HashSet<>();
        Set<InputCoin> usdtCoins = new HashSet<>();
        Set<InputCoin> btcCoins = new HashSet<>();
        Set<InputCoin> bnbCoins = new HashSet<>();

        inputCoins.forEach(e -> {
            switch (e.getQuoteAsset()) {
                case "BUSD":
                    busdCoins.add(e);
                    break;
                case "USDT":
                    usdtCoins.add(e);
                    break;
                case "BTC":
                    btcCoins.add(e);
                    break;
                case "BNB":
                    bnbCoins.add(e);
                    break;
                default:
                    break;
            }

        });
//PROSBUSD
        Set<String> busdCoinAssert = busdCoins
                .stream()
                .map(InputCoin::getBaseAsset)
                .collect(Collectors.toSet());
        Set<String> symbols = busdCoins.stream().map(InputCoin::getSymbol).collect(Collectors.toSet());
//PROS BNB - НЕТ
        Collection<String> coinsFromUsdt = CollectionUtils.subtract(transformInputCoin(usdtCoins), busdCoinAssert);
        busdCoinAssert.addAll(coinsFromUsdt);
        //и в полный список symbols добавить
        symbols.addAll(coinsFromUsdt.stream().map(e -> e + "USDT").collect(Collectors.toSet()));
        Collection<String> coinsFromBtc = CollectionUtils.subtract(transformInputCoin(btcCoins), busdCoinAssert);
        busdCoinAssert.addAll(coinsFromBtc);
        //и в полный список symbols добавить
        symbols.addAll(coinsFromBtc.stream().map(e -> e + "BTC").collect(Collectors.toSet()));
        Collection<String> coinsFromBnb = CollectionUtils.subtract(transformInputCoin(bnbCoins), busdCoinAssert);
        busdCoinAssert.addAll(coinsFromBnb);
        //и в полный список symbols добавить
        symbols.addAll(coinsFromBnb.stream().map(e -> e + "BNB").collect(Collectors.toSet()));

        return symbols;
    }

    public Set<String> getNewSymbols(Set<String> oldSymbols, Set<String> newSymbols) {
        return new HashSet<>(CollectionUtils.subtract(newSymbols, oldSymbols));
    }

    public void setInputCoins(List<InputCoin> inputCoins) {
        this.inputCoins = inputCoins;
    }
}
