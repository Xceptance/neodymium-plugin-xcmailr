package com.xceptance.neodymium.plugin.xcmailr.data;

public class EmailAccount
{
    private String email;

    private String login;

    private String password;

    private String server;

    private int port;

    private boolean ssl;

    private boolean tls;

    public EmailAccount()
    {
    }

    public EmailAccount(String email, String password, String server, int port, boolean ssl, boolean tls)
    {
        this.email = email;
        this.login = email;
        this.password = password;
        this.server = server;
        this.port = port;
        this.ssl = ssl;
        this.tls = tls;
    }

    public EmailAccount(String email, String login, String password, String server, int port, boolean ssl, boolean tls)
    {
        this.email = email;
        this.login = login;
        this.password = password;
        this.server = server;
        this.port = port;
        this.ssl = ssl;
        this.tls = tls;
    }

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getServer()
    {
        return server;
    }

    public void setServer(String server)
    {
        this.server = server;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public boolean isSsl()
    {
        return ssl;
    }

    public void setSsl(boolean ssl)
    {
        this.ssl = ssl;
    }

    public boolean isTls()
    {
        return tls;
    }

    public void setTls(boolean tls)
    {
        this.tls = tls;
    }

    @Override
    public String toString()
    {
        return "EmailAccount [email=" + email + ", login=" + login + ", password=" + password + ", server=" + server + ", port=" + port +
               ", ssl=" + ssl + ", tls=" + tls + "]";
    }
}
