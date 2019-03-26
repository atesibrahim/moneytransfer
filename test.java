package com.ates.moneytransfer;

import com.ates.moneytransfer.model.Account;
import com.ates.moneytransfer.model.Transfer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.Spark;
import spark.utils.IOUtils;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class MoneyTransferAppTest {
    @BeforeClass
    public static void beforeClass() {
        MoneyTransferApp.main(null);
    }

    @AfterClass
    public static void afterClass() {
        Spark.stop();
    }

    @Test
    public void accountShouldBeListed() {
        TestResponse res = request("GET", "/api/accounts","");

        assertEquals(200, res.status);
    }

    @Test
    public void accountShouldBeRead() {
        TestResponse res = request("GET", "/api/accounts/1","");
        Map<String, String> json = res.json();
        assertEquals(200, res.status);
        assertEquals("halil", json.get("name"));
        assertEquals(new Double(2560), json.get("balance"));
        assertNotNull(json.get("accountId"));
    }

    @Test
    public void accountShouldBeRemove() {
        TestResponse res = request("DELETE", "/api/accounts/1", "");
        assertEquals(200, res.status);
    }

    @Test
    public void accountShouldBeUpdate() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        Account account = new Account();
        account.setBalance(BigDecimal.TEN);
        account.setName("test");
        String data = objectMapper.writeValueAsString(account);
        TestResponse res = request("PUT", "/api/accounts/2",data);
        Map<String, String> json = res.json();
        assertEquals(200, res.status);
        assertEquals("test", json.get("name"));
        assertNotNull(json.get("accountId"));
    }

    @Test
    public void accountShouldBeAdd() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        Account account = new Account();
        account.setBalance(BigDecimal.TEN);
        account.setName("test");
        String data = objectMapper.writeValueAsString(account);
        TestResponse res = request("POST", "/api/accounts",data);
        Map<String, String> json = res.json();
        assertEquals(201, res.status);
        assertEquals("test", json.get("name"));
        assertNotNull(account.getAccountId());
    }

    @Test
    public void transferShouldBeListed() {
        TestResponse res = request("GET", "/api/transfers","");

        assertEquals(200, res.status);
    }

    @Test
    public void transferShouldBeRead() {
        TestResponse res = request("GET", "/api/transfers/1","");
        Map<String, String> json = res.json();
        assertEquals(200, res.status);
        assertNotNull(json.get("id"));
    }

    @Test
    public void transferShouldBeAdd() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Transfer transfer = new Transfer();
        transfer.setAmount(BigDecimal.TEN);
        transfer.setFromAccountId(1);
        transfer.setToAccountId(2);
        transfer.setComment("test");
        String data = objectMapper.writeValueAsString(transfer);
        TestResponse res = request("POST", "/api/transfers",data);
        //Map<String, String> json = res.json();
        assertEquals(201, res.status);
        //assertEquals(true, res.body.contains(account.getName()));
    }


    @Test
    public void transferShouldBeUpdate() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        Transfer transfer = new Transfer();
        transfer.setAmount(BigDecimal.TEN);
        transfer.setFromAccountId(1);
        transfer.setToAccountId(0);
        transfer.setComment("test");
        String data = objectMapper.writeValueAsString(transfer);
        TestResponse res = request("PUT", "/api/transfers/1",data);
        //Map<String, String> json = res.json();
        assertEquals(200, res.status);
        assertNotNull(transfer.getId());
    }

    private TestResponse request(String method, String path, String data) {
        try {
            URL url = new URL("http://localhost:8282" + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            if(data!=null&&data!="")  this.sendData(connection, data);

            connection.connect();
            String body = IOUtils.toString(connection.getInputStream());
            TestResponse testResponse =  new TestResponse(connection.getResponseCode(), body);

            return testResponse;
        } catch (IOException e) {
            e.printStackTrace();
            fail("Sending request failed: " + e.getMessage());
            return null;
        }
    }

    protected void sendData(HttpURLConnection con, String data) throws IOException {
        DataOutputStream wr = null;
        try {
            wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(data);
            wr.flush();
            wr.close();
        } catch(IOException exception) {
            throw exception;
        } finally {
            this.closeQuietly(wr);
        }
    }
    protected void closeQuietly(Closeable closeable) {
        try {
            if( closeable != null ) {
                closeable.close();
            }
        } catch(IOException ex) {

        }
    }

    private static class TestResponse {

        public final String body;
        public final int status;

        public TestResponse(int status, String body) {
            this.status = status;
            this.body = body;
        }

        public Map<String,String> json() {
            return new Gson().fromJson(body, HashMap.class);
        }
    }
}
