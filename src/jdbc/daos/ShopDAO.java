package jdbc.daos;



import item.Item;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import shop.ItemShop;
import shop.Shop;
import shop.TabShop;
import nro.services.ItemService;
import nro.server.Manager;
import utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ShopDAO {

    public static List<Shop> getShops(Connection con) {
        List<Shop> list = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement("select * from shop order by npc_id asc");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Shop shop = new Shop();
                shop.id = rs.getInt("id");
                shop.npcId = rs.getByte("npc_id");
                shop.tagName = rs.getString("tag_name");
                shop.typeShop = rs.getByte("type_shop");
                loadShopTab(con, shop);
                list.add(shop);
            }
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
        return list;
    }

    private static void loadShopTab(Connection con, Shop shop) {
        try {
            PreparedStatement ps = con.prepareStatement("select * from tab_shop where shop_id = ? order by tab_index asc");
            ps.setInt(1, shop.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TabShop tab = new TabShop();
                tab.shop = shop;
                tab.id = rs.getInt("id");
                tab.name = rs.getString("tab_name").replaceAll("<>", "\n");
                tab.index = rs.getInt("tab_index");
                loadItemShop(con, tab);
                shop.tabShops.add(tab);
            }
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
    }

    private static void loadItemShop(Connection con, TabShop tabShop) {
        try {
            PreparedStatement ps = con.prepareStatement("select * from tab_shop where tab_index = ? and shop_id = ?");
            ps.setInt(1, tabShop.index);
            ps.setInt(2, tabShop.shop.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                JSONArray dataArray;
                JSONValue jv = new JSONValue();
                JSONObject dataObject;
                dataArray = (JSONArray) jv.parse(rs.getString("items"));
                for (Object o : dataArray) {
                    Item item = null;
                    dataObject = (JSONObject) o;
                    ItemShop itemShop = new ItemShop();
                    itemShop.tabShop = tabShop;
                    itemShop.id = tabShop.itemShops.size() + 1;
                    int tempId = getInt(dataObject, "temp_id", -1);
                    if (tempId < 0 || tempId >= Manager.ITEM_TEMPLATES.size() || Manager.ITEM_TEMPLATES.get(tempId) == null) {
                        Logger.error("Bỏ qua item shop thiếu template: shop_id=" + tabShop.shop.id
                                + ", tab_id=" + tabShop.id + ", temp_id=" + tempId + "\n");
                        continue;
                    }
                    itemShop.temp = ItemService.gI().getTemplate(tempId);
                    itemShop.isNew = Boolean.parseBoolean(String.valueOf(dataObject.get("is_new")));
                    itemShop.cost = getInt(dataObject, "cost", 0);
                    itemShop.iconSpec = getInt(dataObject, "item_spec", 0);
                    itemShop.typeSell = (byte) getInt(dataObject, "type_sell", 0);
                    JSONArray options = (JSONArray) dataObject.get("options");
                    if (options != null) {
                        for (int j = 0; j < options.size(); j++) {
                            JSONObject opt = (JSONObject) options.get(j);
                            itemShop.options.add(new Item.ItemOption(getInt(opt, "id", 0), getInt(opt, "param", 0)));
                        }
                    }
                    ItemService.gI().normalizeItemShopOptions(itemShop);
                    boolean isSell = Boolean.parseBoolean(String.valueOf(dataObject.get("is_sell")));
                    if (isSell) {
                        tabShop.itemShops.add(itemShop);
                    }
                }
            }
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
    }

    private static int getInt(JSONObject object, String key, int defaultValue) {
        Object value = object.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty() || text.equalsIgnoreCase("null")) {
            return defaultValue;
        }
        return Integer.parseInt(text);
    }

    private static void loadItemShopOption(Connection con, ItemShop itemShop) {
        try {
            PreparedStatement ps = con.prepareStatement("select * from item_shop_option where item_shop_id = ?");
            ps.setInt(1, itemShop.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                itemShop.options.add(new Item.ItemOption(rs.getInt("option_id"), rs.getInt("param")));
            }
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
            }
        } catch (Exception e) {
            Logger.logException(ShopDAO.class, e);
        }
    }

}
