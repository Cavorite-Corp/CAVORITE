package nxt.http;

import nxt.NxtException;
import nxt.Trade;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

public final class GetAllTrades extends APIServlet.APIRequestHandler {

    static final GetAllTrades instance = new GetAllTrades();

    private GetAllTrades() {
        super("timestamp");
    }
    
    @Override
    JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        int timestamp = ParameterParser.getTimestamp(req);
        JSONObject response = new JSONObject();
        JSONArray tradesData = new JSONArray();
        Collection<Trade> trades = Trade.getAllTrades();
        for (Trade trade : trades) {
            if (trade.getTimestamp() >= timestamp) {
                tradesData.add(JSONData.trade(trade));
            }
        }
        response.put("trades", tradesData);
        return response;
    }

}
