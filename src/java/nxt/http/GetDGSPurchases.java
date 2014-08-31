package nxt.http;

import nxt.DigitalGoodsStore;
import nxt.NxtException;
import nxt.db.DbIterator;
import nxt.db.DbUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetDGSPurchases extends APIServlet.APIRequestHandler {

    static final GetDGSPurchases instance = new GetDGSPurchases();

    private GetDGSPurchases() {
        super(new APITag[] {APITag.DGS}, "seller", "buyer", "firstIndex", "lastIndex", "completed");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        Long sellerId = ParameterParser.getSellerId(req);
        Long buyerId = ParameterParser.getBuyerId(req);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean completed = "true".equalsIgnoreCase(req.getParameter("completed"));


        JSONObject response = new JSONObject();
        JSONArray purchasesJSON = new JSONArray();
        response.put("purchases", purchasesJSON);

        if (sellerId == null && buyerId == null) {
            try (DbIterator<DigitalGoodsStore.Purchase> purchases = DigitalGoodsStore.getAllPurchases(0, -1)) {
                int count = 0;
                while (purchases.hasNext() && count <= lastIndex) {
                    DigitalGoodsStore.Purchase purchase = purchases.next();
                    if (! (completed && purchase.isPending())) {
                        if (count >= firstIndex) {
                            purchasesJSON.add(JSONData.purchase(purchase));
                        }
                        count++;
                    }
                }
            }
            return response;
        }

        DbIterator<DigitalGoodsStore.Purchase> purchases = null;
        try {
            if (sellerId != null && buyerId == null) {
                purchases = DigitalGoodsStore.getSellerPurchases(sellerId, 0, -1);
            } else if (sellerId == null) {
                purchases = DigitalGoodsStore.getBuyerPurchases(buyerId, 0, -1);
            } else {
                purchases = DigitalGoodsStore.getSellerBuyerPurchases(sellerId, buyerId, 0, -1);
            }
            int count = 0;
            while (purchases.hasNext() && count <= lastIndex) {
                DigitalGoodsStore.Purchase purchase = purchases.next();
                if (! (completed && purchase.isPending())) {
                    if (count >= firstIndex) {
                        purchasesJSON.add(JSONData.purchase(purchase));
                    }
                    count++;
                }
            }
        } finally {
            DbUtils.close(purchases);
        }
        return response;
    }

}
