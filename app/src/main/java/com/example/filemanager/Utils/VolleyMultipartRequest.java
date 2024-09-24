package com.example.filemanager.Utils;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;
    private final Map<String, String> headers;

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.headers = new HashMap<>();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return null; // Params will be handled in `getByteData()`
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // Adding text parameters and file data
            if (getParams() != null && !getParams().isEmpty()) {
                for (Map.Entry<String, String> entry : getParams().entrySet()) {
                    appendFormField(bos, entry.getKey(), entry.getValue());
                }
            }

            if (getByteData() != null && !getByteData().isEmpty()) {
                for (Map.Entry<String, DataPart> entry : getByteData().entrySet()) {
                    appendFileData(bos, entry.getKey(), entry.getValue());
                }
            }

            bos.write(("--" + boundary + "--\r\n").getBytes()); // end of multipart/form-data
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bos.toByteArray();
    }

    // This method allows you to add form fields
    private void appendFormField(ByteArrayOutputStream bos, String name, String value) throws IOException {
        bos.write(("--" + boundary + "\r\n").getBytes());
        bos.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n").getBytes());
        bos.write(("\r\n").getBytes());
        bos.write((value + "\r\n").getBytes());
    }

    // This method allows you to add file data
    private void appendFileData(ByteArrayOutputStream bos, String name, DataPart dataFile) throws IOException {
        bos.write(("--" + boundary + "\r\n").getBytes());
        bos.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + dataFile.getFileName() + "\"\r\n").getBytes());
        bos.write(("Content-Type: application/octet-stream\r\n").getBytes());
        bos.write(("\r\n").getBytes());
        bos.write(dataFile.getData());
        bos.write(("\r\n").getBytes());
    }

    // Override this method to handle file data
    protected Map<String, DataPart> getByteData() throws AuthFailureError {
        return null;
    }

    private final String boundary = "apiclient-" + System.currentTimeMillis();

    public static class DataPart {
        private final String fileName;
        private final byte[] data;

        public DataPart(String fileName, byte[] data) {
            this.fileName = fileName;
            this.data = data;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getData() {
            return data;
        }
    }
}
