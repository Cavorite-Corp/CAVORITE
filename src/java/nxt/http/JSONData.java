package nxt.http;

import nxt.Account;
import nxt.Alias;
import nxt.Appendix;
import nxt.Asset;
import nxt.AssetTransfer;
import nxt.Attachment;
import nxt.Block;
import nxt.Constants;
import nxt.Currency;
import nxt.CurrencyExchangeOffer;
import nxt.CurrencyFounder;
import nxt.CurrencyTransfer;
import nxt.CurrencyType;
import nxt.DigitalGoodsStore;
import nxt.Exchange;
import nxt.Generator;
import nxt.Nxt;
import nxt.Order;
import nxt.PhasingPoll;
import nxt.PhasingVote;
import nxt.Poll;
import nxt.Token;
import nxt.Trade;
import nxt.Transaction;
import nxt.Vote;
import nxt.VoteWeighting;
import nxt.crypto.Crypto;
import nxt.crypto.EncryptedData;
import nxt.peer.Hallmark;
import nxt.peer.Peer;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Map;

final class JSONData {

    static JSONObject alias(Alias alias) {
        JSONObject json = new JSONObject();
        putAccount(json, "account", alias.getAccountId());
        json.put("aliasName", alias.getAliasName());
        json.put("aliasURI", alias.getAliasURI());
        json.put("timestamp", alias.getTimestamp());
        json.put("alias", Long.toUnsignedString(alias.getId()));
        Alias.Offer offer = Alias.getOffer(alias);
        if (offer != null) {
            json.put("priceNQT", String.valueOf(offer.getPriceNQT()));
            if (offer.getBuyerId() != 0) {
                json.put("buyer", Long.toUnsignedString(offer.getBuyerId()));
            }
        }
        return json;
    }

    static JSONObject accountBalance(Account account) {
        JSONObject json = new JSONObject();
        if (account == null) {
            json.put("balanceNQT", "0");
            json.put("unconfirmedBalanceNQT", "0");
            json.put("effectiveBalanceNXT", "0");
            json.put("forgedBalanceNQT", "0");
            json.put("guaranteedBalanceNQT", "0");
        } else {
            json.put("balanceNQT", String.valueOf(account.getBalanceNQT()));
            json.put("unconfirmedBalanceNQT", String.valueOf(account.getUnconfirmedBalanceNQT()));
            json.put("effectiveBalanceNXT", account.getEffectiveBalanceNXT());
            json.put("forgedBalanceNQT", String.valueOf(account.getForgedBalanceNQT()));
            json.put("guaranteedBalanceNQT", String.valueOf(account.getGuaranteedBalanceNQT()));
        }
        return json;
    }

    static JSONObject lessor(Account account) {
        JSONObject json = new JSONObject();
        json.put("currentLesseeRS", Convert.rsAccount(account.getCurrentLesseeId()));
        json.put("currentHeightFrom", String.valueOf(account.getCurrentLeasingHeightFrom()));
        json.put("currentHeightTo", String.valueOf(account.getCurrentLeasingHeightTo()));
        json.put("nextLesseeRS", Convert.rsAccount(account.getNextLesseeId()));
        json.put("nextHeightFrom", String.valueOf(account.getNextLeasingHeightFrom()));
        json.put("nextHeightTo", String.valueOf(account.getNextLeasingHeightTo()));
        json.put("effectiveBalanceNXT", String.valueOf(account.getGuaranteedBalanceNQT() / Constants.ONE_NXT));
        return json;
    }

    static JSONObject asset(Asset asset, boolean includeCounts) {
        JSONObject json = new JSONObject();
        putAccount(json, "account", asset.getAccountId());
        json.put("name", asset.getName());
        json.put("description", asset.getDescription());
        json.put("decimals", asset.getDecimals());
        json.put("quantityQNT", String.valueOf(asset.getQuantityQNT()));
        json.put("asset", Long.toUnsignedString(asset.getId()));
        if (includeCounts) {
            json.put("numberOfTrades", Trade.getTradeCount(asset.getId()));
            json.put("numberOfTransfers", AssetTransfer.getTransferCount(asset.getId()));
            json.put("numberOfAccounts", Account.getAssetAccountCount(asset.getId()));
        }
        return json;
    }

    static JSONObject currency(Currency currency, boolean includeCounts) {
        JSONObject json = new JSONObject();
        json.put("currency", Long.toUnsignedString(currency.getId()));
        putAccount(json, "account", currency.getAccountId());
        json.put("name", currency.getName());
        json.put("code", currency.getCode());
        json.put("description", currency.getDescription());
        json.put("type", currency.getType());
        json.put("initialSupply", String.valueOf(currency.getInitialSupply()));
        json.put("currentSupply", String.valueOf(currency.getCurrentSupply()));
        json.put("reserveSupply", String.valueOf(currency.getReserveSupply()));
        json.put("maxSupply", String.valueOf(currency.getMaxSupply()));
        json.put("creationHeight", currency.getCreationHeight());
        json.put("issuanceHeight", currency.getIssuanceHeight());
        json.put("minReservePerUnitNQT", String.valueOf(currency.getMinReservePerUnitNQT()));
        json.put("currentReservePerUnitNQT", String.valueOf(currency.getCurrentReservePerUnitNQT()));
        json.put("minDifficulty", currency.getMinDifficulty());
        json.put("maxDifficulty", currency.getMaxDifficulty());
        json.put("algorithm", currency.getAlgorithm());
        json.put("decimals", currency.getDecimals());
        if (includeCounts) {
            json.put("numberOfExchanges", Exchange.getExchangeCount(currency.getId()));
            json.put("numberOfTransfers", CurrencyTransfer.getTransferCount(currency.getId()));
        }
        JSONArray types = new JSONArray();
        for (CurrencyType type : CurrencyType.values()) {
            if (currency.is(type)) {
                types.add(type.toString());
            }
        }
        json.put("types", types);
        return json;
    }

    static JSONObject currencyFounder(CurrencyFounder founder) {
        JSONObject json = new JSONObject();
        json.put("currency", Long.toUnsignedString(founder.getCurrencyId()));
        putAccount(json, "account", founder.getAccountId());
        json.put("amountPerUnitNQT", String.valueOf(founder.getAmountPerUnitNQT()));
        return json;
    }

    static JSONObject accountAsset(Account.AccountAsset accountAsset, boolean includeAccount, boolean includeAssetInfo) {
        JSONObject json = new JSONObject();
        if (includeAccount) {
            putAccount(json, "account", accountAsset.getAccountId());
        }
        json.put("asset", Long.toUnsignedString(accountAsset.getAssetId()));
        json.put("quantityQNT", String.valueOf(accountAsset.getQuantityQNT()));
        json.put("unconfirmedQuantityQNT", String.valueOf(accountAsset.getUnconfirmedQuantityQNT()));
        if (includeAssetInfo) {
            putAssetInfo(json, accountAsset.getAssetId());
        }
        return json;
    }

    static JSONObject accountCurrency(Account.AccountCurrency accountCurrency, boolean includeAccount, boolean includeCurrencyInfo) {
        JSONObject json = new JSONObject();
        if (includeAccount) {
            putAccount(json, "account", accountCurrency.getAccountId());
        }
        json.put("currency", Long.toUnsignedString(accountCurrency.getCurrencyId()));
        json.put("units", String.valueOf(accountCurrency.getUnits()));
        json.put("unconfirmedUnits", String.valueOf(accountCurrency.getUnconfirmedUnits()));
        if (includeCurrencyInfo) {
            putCurrencyInfo(json, accountCurrency.getCurrencyId());
        }
        return json;
    }

    static JSONObject askOrder(Order.Ask order) {
        JSONObject json = order(order);
        json.put("type", "ask");
        return json;
    }

    static JSONObject bidOrder(Order.Bid order) {
        JSONObject json = order(order);
        json.put("type", "bid");
        return json;
    }

    static JSONObject order(Order order) {
        JSONObject json = new JSONObject();
        json.put("order", Long.toUnsignedString(order.getId()));
        json.put("asset", Long.toUnsignedString(order.getAssetId()));
        putAccount(json, "account", order.getAccountId());
        json.put("quantityQNT", String.valueOf(order.getQuantityQNT()));
        json.put("priceNQT", String.valueOf(order.getPriceNQT()));
        json.put("height", order.getHeight());
        json.put("transactionIndex", order.getTransactionIndex());
        json.put("transactionHeight", order.getTransactionHeight());
        return json;
    }

    static JSONObject offer(CurrencyExchangeOffer offer) {
        JSONObject json = new JSONObject();
        json.put("offer", Long.toUnsignedString(offer.getId()));
        putAccount(json, "account", offer.getAccountId());
        json.put("height", offer.getHeight());
        json.put("expirationHeight", offer.getExpirationHeight());
        json.put("currency", Long.toUnsignedString(offer.getCurrencyId()));
        json.put("rateNQT", String.valueOf(offer.getRateNQT()));
        json.put("limit", String.valueOf(offer.getLimit()));
        json.put("supply", String.valueOf(offer.getSupply()));
        return json;
    }

    static JSONObject block(Block block, boolean includeTransactions) {
        JSONObject json = new JSONObject();
        json.put("block", block.getStringId());
        json.put("height", block.getHeight());
        putAccount(json, "generator", block.getGeneratorId());
        json.put("generatorPublicKey", Convert.toHexString(block.getGeneratorPublicKey()));
        json.put("timestamp", block.getTimestamp());
        json.put("numberOfTransactions", block.getTransactions().size());
        json.put("totalAmountNQT", String.valueOf(block.getTotalAmountNQT()));
        json.put("totalFeeNQT", String.valueOf(block.getTotalFeeNQT()));
        json.put("payloadLength", block.getPayloadLength());
        json.put("version", block.getVersion());
        json.put("baseTarget", Long.toUnsignedString(block.getBaseTarget()));
        json.put("cumulativeDifficulty", block.getCumulativeDifficulty().toString());
        if (block.getPreviousBlockId() != 0) {
            json.put("previousBlock", Long.toUnsignedString(block.getPreviousBlockId()));
        }
        if (block.getNextBlockId() != 0) {
            json.put("nextBlock", Long.toUnsignedString(block.getNextBlockId()));
        }
        json.put("payloadHash", Convert.toHexString(block.getPayloadHash()));
        json.put("generationSignature", Convert.toHexString(block.getGenerationSignature()));
        if (block.getVersion() > 1) {
            json.put("previousBlockHash", Convert.toHexString(block.getPreviousBlockHash()));
        }
        json.put("blockSignature", Convert.toHexString(block.getBlockSignature()));
        JSONArray transactions = new JSONArray();
        for (Transaction transaction : block.getTransactions()) {
            transactions.add(includeTransactions ? transaction(transaction) : Long.toUnsignedString(transaction.getId()));
        }
        json.put("transactions", transactions);
        return json;
    }

    static JSONObject encryptedData(EncryptedData encryptedData) {
        JSONObject json = new JSONObject();
        json.put("data", Convert.toHexString(encryptedData.getData()));
        json.put("nonce", Convert.toHexString(encryptedData.getNonce()));
        return json;
    }

    static JSONObject goods(DigitalGoodsStore.Goods goods, boolean includeCounts) {
        JSONObject json = new JSONObject();
        json.put("goods", Long.toUnsignedString(goods.getId()));
        json.put("name", goods.getName());
        json.put("description", goods.getDescription());
        json.put("quantity", goods.getQuantity());
        json.put("priceNQT", String.valueOf(goods.getPriceNQT()));
        putAccount(json, "seller", goods.getSellerId());
        json.put("tags", goods.getTags());
        JSONArray tagsJSON = new JSONArray();
        Collections.addAll(tagsJSON, goods.getParsedTags());
        json.put("parsedTags", tagsJSON);
        json.put("delisted", goods.isDelisted());
        json.put("timestamp", goods.getTimestamp());
        if (includeCounts) {
            json.put("numberOfPurchases", DigitalGoodsStore.Purchase.getGoodsPurchaseCount(goods.getId(), false, true));
            json.put("numberOfPublicFeedbacks", DigitalGoodsStore.Purchase.getGoodsPurchaseCount(goods.getId(), true, true));
        }
        return json;
    }

    static JSONObject tag(DigitalGoodsStore.Tag tag) {
        JSONObject json = new JSONObject();
        json.put("tag", tag.getTag());
        json.put("inStockCount", tag.getInStockCount());
        json.put("totalCount", tag.getTotalCount());
        return json;
    }

    static JSONObject hallmark(Hallmark hallmark) {
        JSONObject json = new JSONObject();
        putAccount(json, "account", Account.getId(hallmark.getPublicKey()));
        json.put("host", hallmark.getHost());
        json.put("weight", hallmark.getWeight());
        String dateString = Hallmark.formatDate(hallmark.getDate());
        json.put("date", dateString);
        json.put("valid", hallmark.isValid());
        return json;
    }

    static JSONObject token(Token token) {
        JSONObject json = new JSONObject();
        putAccount(json, "account", Account.getId(token.getPublicKey()));
        json.put("timestamp", token.getTimestamp());
        json.put("valid", token.isValid());
        return json;
    }

    static JSONObject peer(Peer peer) {
        JSONObject json = new JSONObject();
        json.put("address", peer.getPeerAddress());
        json.put("state", peer.getState().ordinal());
        json.put("announcedAddress", peer.getAnnouncedAddress());
        json.put("shareAddress", peer.shareAddress());
        if (peer.getHallmark() != null) {
            json.put("hallmark", peer.getHallmark().getHallmarkString());
        }
        json.put("weight", peer.getWeight());
        json.put("downloadedVolume", peer.getDownloadedVolume());
        json.put("uploadedVolume", peer.getUploadedVolume());
        json.put("application", peer.getApplication());
        json.put("version", peer.getVersion());
        json.put("platform", peer.getPlatform());
        json.put("blacklisted", peer.isBlacklisted());
        json.put("lastUpdated", peer.getLastUpdated());
        if (peer.isBlacklisted()) {
            json.put("blacklistingCause", peer.getBlacklistingCause());
        }
        return json;
    }

    static JSONObject poll(Poll poll) {
        JSONObject json = new JSONObject();
        putAccount(json, "account", poll.getAccountId());
        json.put("poll", Long.toUnsignedString(poll.getId()));
        json.put("name", poll.getName());
        json.put("description", poll.getDescription());
        JSONArray options = new JSONArray();
        Collections.addAll(options, poll.getOptions());
        json.put("options", options);
        json.put("finishHeight", poll.getFinishHeight());
        json.put("minNumberOfOptions", poll.getMinNumberOfOptions());
        json.put("maxNumberOfOptions", poll.getMaxNumberOfOptions());
        json.put("minRangeValue", poll.getMinRangeValue());
        json.put("maxRangeValue", poll.getMaxRangeValue());
        putVoteWeighting(json, poll.getVoteWeighting());
        json.put("finished", poll.isFinished());
        return json;
    }

    static JSONObject pollResults(Poll poll, List<Poll.OptionResult> results) {
        JSONObject json = new JSONObject();
        json.put("poll", Long.toUnsignedString(poll.getId()));
        if (poll.getVoteWeighting().getMinBalanceModel() == VoteWeighting.MinBalanceModel.ASSET) {
            long holdingId = poll.getVoteWeighting().getHoldingId();
            json.put("holding", Long.toUnsignedString(holdingId));
            json.put("decimals", Asset.getAsset(holdingId).getDecimals());
        } else if(poll.getVoteWeighting().getMinBalanceModel() == VoteWeighting.MinBalanceModel.CURRENCY) {
            long holdingId = poll.getVoteWeighting().getHoldingId();
            json.put("holding", Long.toUnsignedString(holdingId));
            json.put("decimals", Currency.getCurrency(holdingId).getDecimals());
        }
        json.put("finished", poll.isFinished());
        JSONArray options = new JSONArray();
        Collections.addAll(options, poll.getOptions());
        json.put("options", options);

        JSONArray resultsJson = new JSONArray();
        for (Poll.OptionResult option : results) {
            JSONObject optionJSON = new JSONObject();
            if (option != null) {
                optionJSON.put("result", String.valueOf(option.getResult()));
                optionJSON.put("weight", String.valueOf(option.getWeight()));
            } else {
                optionJSON.put("result", "");
                optionJSON.put("weight", 0);
            }
            resultsJson.add(optionJSON);
        }
        json.put("results", resultsJson);
        return json;
    }

    static JSONObject vote(Vote vote){
        JSONObject json = new JSONObject();
        putAccount(json, "voter", vote.getVoterId());
        json.put("transaction", Long.toUnsignedString(vote.getId()));
        JSONArray votesJson = new JSONArray();
        for (byte v : vote.getVoteBytes()) {
            if (v == Constants.NO_VOTE_VALUE) {
                votesJson.add("");
            } else {
                votesJson.add(Byte.toString(v));
            }
        }
        json.put("votes", votesJson);
        return json;
    }

    static JSONObject phasingPoll(PhasingPoll poll, boolean countVotes) {
        JSONObject json = new JSONObject();
        json.put("transaction", Long.toUnsignedString(poll.getId()));
        json.put("transactionFullHash", Convert.toHexString(poll.getFullHash()));
        json.put("finished", poll.isFinished());
        json.put("finishHeight", poll.getFinishHeight());
        json.put("quorum", String.valueOf(poll.getQuorum()));
        putAccount(json, "account", poll.getAccountId());
        JSONArray whitelistJson = new JSONArray();
        for (long accountId : poll.getWhitelist()) {
            JSONObject whitelisted = new JSONObject();
            putAccount(whitelisted, "whitelisted", accountId);
            whitelistJson.add(whitelisted);
        }
        json.put("whitelist", whitelistJson);
        if (poll.getLinkedFullHashes().length > 0) {
            JSONArray linkedFullHashesJSON = new JSONArray();
            for (byte[] hash : poll.getLinkedFullHashes()) {
                linkedFullHashesJSON.add(Convert.toHexString(hash));
            }
            json.put("linkedFullHashes", linkedFullHashesJSON);
        }
        if (poll.getHashedSecret() != null) {
            json.put("hashedSecret", Convert.toHexString(poll.getHashedSecret()));
        }
        putVoteWeighting(json, poll.getVoteWeighting());
        if (poll.isFinished()) {
            PhasingPoll.PhasingPollResult phasingPollResult = PhasingPoll.getResult(poll.getId());
            if (phasingPollResult != null) {
                json.put("approved", phasingPollResult.isApproved());
                json.put("result", String.valueOf(phasingPollResult.getResult()));
            }
        } else if (countVotes) {
            json.put("result", String.valueOf(poll.getResult()));
        }
        return json;
    }

    static JSONObject phasingPollResult(PhasingPoll.PhasingPollResult phasingPollResult) {
        JSONObject json = new JSONObject();
        json.put("transaction", Long.toUnsignedString(phasingPollResult.getId()));
        json.put("approved", phasingPollResult.isApproved());
        json.put("result", String.valueOf(phasingPollResult.getResult()));
        return json;
    }

    static JSONObject phasingPollVote(PhasingVote vote) {
        JSONObject json = new JSONObject();
        JSONData.putAccount(json, "voter", vote.getVoterId());
        json.put("transaction", Long.toUnsignedString(vote.getVoteId()));
        return json;
    }

    private static void putVoteWeighting(JSONObject json, VoteWeighting voteWeighting) {
        json.put("votingModel", voteWeighting.getVotingModel().getCode());
        json.put("minBalance", String.valueOf(voteWeighting.getMinBalance()));
        json.put("minBalanceModel", voteWeighting.getMinBalanceModel().getCode());
        if (voteWeighting.getHoldingId() != 0) {
            json.put("holding", Long.toUnsignedString(voteWeighting.getHoldingId()));
        }
    }

    static JSONObject purchase(DigitalGoodsStore.Purchase purchase) {
        JSONObject json = new JSONObject();
        json.put("purchase", Long.toUnsignedString(purchase.getId()));
        json.put("goods", Long.toUnsignedString(purchase.getGoodsId()));
        json.put("name", purchase.getName());
        putAccount(json, "seller", purchase.getSellerId());
        json.put("priceNQT", String.valueOf(purchase.getPriceNQT()));
        json.put("quantity", purchase.getQuantity());
        putAccount(json, "buyer", purchase.getBuyerId());
        json.put("timestamp", purchase.getTimestamp());
        json.put("deliveryDeadlineTimestamp", purchase.getDeliveryDeadlineTimestamp());
        if (purchase.getNote() != null) {
            json.put("note", encryptedData(purchase.getNote()));
        }
        json.put("pending", purchase.isPending());
        if (purchase.getEncryptedGoods() != null) {
            json.put("goodsData", encryptedData(purchase.getEncryptedGoods()));
            json.put("goodsIsText", purchase.goodsIsText());
        }
        if (purchase.getFeedbackNotes() != null) {
            JSONArray feedbacks = new JSONArray();
            for (EncryptedData encryptedData : purchase.getFeedbackNotes()) {
                feedbacks.add(0, encryptedData(encryptedData));
            }
            json.put("feedbackNotes", feedbacks);
        }
        if (purchase.getPublicFeedbacks() != null) {
            JSONArray publicFeedbacks = new JSONArray();
            for (String publicFeedback : purchase.getPublicFeedbacks()) {
                publicFeedbacks.add(0, publicFeedback);
            }
            json.put("publicFeedbacks", publicFeedbacks);
        }
        if (purchase.getRefundNote() != null) {
            json.put("refundNote", encryptedData(purchase.getRefundNote()));
        }
        if (purchase.getDiscountNQT() > 0) {
            json.put("discountNQT", String.valueOf(purchase.getDiscountNQT()));
        }
        if (purchase.getRefundNQT() > 0) {
            json.put("refundNQT", String.valueOf(purchase.getRefundNQT()));
        }
        return json;
    }

    static JSONObject trade(Trade trade, boolean includeAssetInfo) {
        JSONObject json = new JSONObject();
        json.put("timestamp", trade.getTimestamp());
        json.put("quantityQNT", String.valueOf(trade.getQuantityQNT()));
        json.put("priceNQT", String.valueOf(trade.getPriceNQT()));
        json.put("asset", Long.toUnsignedString(trade.getAssetId()));
        json.put("askOrder", Long.toUnsignedString(trade.getAskOrderId()));
        json.put("bidOrder", Long.toUnsignedString(trade.getBidOrderId()));
        json.put("askOrderHeight", trade.getAskOrderHeight());
        json.put("bidOrderHeight", trade.getBidOrderHeight());
        putAccount(json, "seller", trade.getSellerId());
        putAccount(json, "buyer", trade.getBuyerId());
        json.put("block", Long.toUnsignedString(trade.getBlockId()));
        json.put("height", trade.getHeight());
        json.put("tradeType", trade.isBuy() ? "buy" : "sell");
        if (includeAssetInfo) {
            putAssetInfo(json, trade.getAssetId());
        }
        return json;
    }

    static JSONObject assetTransfer(AssetTransfer assetTransfer, boolean includeAssetInfo) {
        JSONObject json = new JSONObject();
        json.put("assetTransfer", Long.toUnsignedString(assetTransfer.getId()));
        json.put("asset", Long.toUnsignedString(assetTransfer.getAssetId()));
        putAccount(json, "sender", assetTransfer.getSenderId());
        putAccount(json, "recipient", assetTransfer.getRecipientId());
        json.put("quantityQNT", String.valueOf(assetTransfer.getQuantityQNT()));
        json.put("height", assetTransfer.getHeight());
        json.put("timestamp", assetTransfer.getTimestamp());
        if (includeAssetInfo) {
            putAssetInfo(json, assetTransfer.getAssetId());
        }
        return json;
    }

    static JSONObject currencyTransfer(CurrencyTransfer transfer, boolean includeCurrencyInfo) {
        JSONObject json = new JSONObject();
        json.put("transfer", Long.toUnsignedString(transfer.getId()));
        json.put("currency", Long.toUnsignedString(transfer.getCurrencyId()));
        putAccount(json, "sender", transfer.getSenderId());
        putAccount(json, "recipient", transfer.getRecipientId());
        json.put("units", String.valueOf(transfer.getUnits()));
        json.put("height", transfer.getHeight());
        json.put("timestamp", transfer.getTimestamp());
        if (includeCurrencyInfo) {
            putCurrencyInfo(json, transfer.getCurrencyId());
        }
        return json;
    }

    static JSONObject exchange(Exchange exchange, boolean includeCurrencyInfo) {
        JSONObject json = new JSONObject();
        json.put("transaction", Long.toUnsignedString(exchange.getTransactionId()));
        json.put("timestamp", exchange.getTimestamp());
        json.put("units", String.valueOf(exchange.getUnits()));
        json.put("rateNQT", String.valueOf(exchange.getRate()));
        json.put("currency", Long.toUnsignedString(exchange.getCurrencyId()));
        json.put("offer", Long.toUnsignedString(exchange.getOfferId()));
        putAccount(json, "seller", exchange.getSellerId());
        putAccount(json, "buyer", exchange.getBuyerId());
        json.put("block", Long.toUnsignedString(exchange.getBlockId()));
        json.put("height", exchange.getHeight());
        if (includeCurrencyInfo) {
            putCurrencyInfo(json, exchange.getCurrencyId());
        }
        return json;
    }

    static JSONObject exchangeRequest(Transaction transaction, boolean includeCurrencyInfo) {
        JSONObject json = new JSONObject();
        json.put("transaction", Long.toUnsignedString(transaction.getId()));
        json.put("subtype", transaction.getType().getSubtype());
        Attachment.MonetarySystemExchange attachment = (Attachment.MonetarySystemExchange) transaction.getAttachment();
        json.put("timestamp", transaction.getTimestamp());
        json.put("units", String.valueOf(attachment.getUnits()));
        json.put("rateNQT", String.valueOf(attachment.getRateNQT()));
        if (includeCurrencyInfo) {
            putCurrencyInfo(json, attachment.getCurrencyId());
        }
        return json;
    }

    static JSONObject unconfirmedTransaction(Transaction transaction) {
        JSONObject json = new JSONObject();
        json.put("type", transaction.getType().getType());
        json.put("subtype", transaction.getType().getSubtype());
        json.put("phased", transaction.getPhasing() != null);
        json.put("timestamp", transaction.getTimestamp());
        json.put("deadline", transaction.getDeadline());
        json.put("senderPublicKey", Convert.toHexString(transaction.getSenderPublicKey()));
        if (transaction.getRecipientId() != 0) {
            putAccount(json, "recipient", transaction.getRecipientId());
        }
        json.put("amountNQT", String.valueOf(transaction.getAmountNQT()));
        json.put("feeNQT", String.valueOf(transaction.getFeeNQT()));
        if (transaction.getReferencedTransactionFullHash() != null) {
            json.put("referencedTransactionFullHash", transaction.getReferencedTransactionFullHash());
        }
        byte[] signature = Convert.emptyToNull(transaction.getSignature());
        if (signature != null) {
            json.put("signature", Convert.toHexString(signature));
            json.put("signatureHash", Convert.toHexString(Crypto.sha256().digest(signature)));
            json.put("fullHash", transaction.getFullHash());
            json.put("transaction", transaction.getStringId());
        }
        JSONObject attachmentJSON = new JSONObject();
        for (Appendix appendage : transaction.getAppendages()) {
            attachmentJSON.putAll(appendage.getJSONObject());
        }
        if (! attachmentJSON.isEmpty()) {
            for (Map.Entry entry : (Iterable<Map.Entry>) attachmentJSON.entrySet()) {
                if (entry.getValue() instanceof Long) {
                    entry.setValue(String.valueOf(entry.getValue()));
                }
            }
            json.put("attachment", attachmentJSON);
        }
        putAccount(json, "sender", transaction.getSenderId());
        json.put("height", transaction.getHeight());
        json.put("version", transaction.getVersion());
        if (transaction.getVersion() > 0) {
            json.put("ecBlockId", Long.toUnsignedString(transaction.getECBlockId()));
            json.put("ecBlockHeight", transaction.getECBlockHeight());
        }

        return json;
    }

    static JSONObject transaction(Transaction transaction) {
        JSONObject json = unconfirmedTransaction(transaction);
        json.put("block", Long.toUnsignedString(transaction.getBlockId()));
        json.put("confirmations", Nxt.getBlockchain().getHeight() - transaction.getHeight());
        json.put("blockTimestamp", transaction.getBlockTimestamp());
        json.put("transactionIndex", transaction.getIndex());
        return json;
    }

    static JSONObject generator(Generator generator, int elapsedTime) {
        JSONObject response = new JSONObject();
        long deadline = generator.getDeadline();
        putAccount(response, "account", generator.getAccountId());
        response.put("deadline", deadline);
        response.put("hitTime", generator.getHitTime());
        response.put("remaining", Math.max(deadline - elapsedTime, 0));
        return response;
    }

    static void putException(JSONObject json, Exception e) {
        putException(json, e, "");
    }

    static void putException(JSONObject json, Exception e, String error) {
        json.put("errorCode", 4);
        if (error.length() > 0) {
            error += ": ";
        }
        json.put("error", e.toString());
        json.put("errorDescription", error + e.getMessage());
    }

    static void putAccount(JSONObject json, String name, long accountId) {
        json.put(name, Long.toUnsignedString(accountId));
        json.put(name + "RS", Convert.rsAccount(accountId));
    }

    private static void putCurrencyInfo(JSONObject json, long currencyId) {
        Currency currency = Currency.getCurrency(currencyId);
        if (currency == null) {
            return;
        }
        json.put("name", currency.getName());
        json.put("code", currency.getCode());
        json.put("type", currency.getType());
        json.put("decimals", currency.getDecimals());
        json.put("issuanceHeight", currency.getIssuanceHeight());
        putAccount(json, "issuerAccount", currency.getAccountId());
    }

    private static void putAssetInfo(JSONObject json, long assetId) {
        Asset asset = Asset.getAsset(assetId);
        json.put("name", asset.getName());
        json.put("decimals", asset.getDecimals());
    }

    private JSONData() {} // never

}
