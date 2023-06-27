package org.openhab.binding.honeywellhome.client.api.pojo;

import java.util.ArrayList;

public class User {
    public int userID;
    public String username;
    public String firstname;
    public String lastname;
    public long created;
    public long deleted;
    public boolean activated;
    public boolean connectedHomeAccountExists;
    public ArrayList<LocationRoleMapping> locationRoleMapping;
    public String isOptOut;
    public boolean isCurrentUser;
}
