package com.example.filemanager.Utils;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> mListener;
    private final Map<String, String> headers;

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.headers = new HashMap<>();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected Map<String, String> getParams() {
        return null;
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

    protected Map<String, DataPart> getByteData() {
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


    public static class CountingOutputStream extends OutputStream {
        private final OutputStream out;
        private final ProgressCallback callback;
        private long bytesWritten = 0;

        public CountingOutputStream(OutputStream out, ProgressCallback callback) {
            this.out = out;
            this.callback = callback;
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            bytesWritten++;
            if (callback != null) {
                callback.onProgress(bytesWritten, out.toString().length());
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            bytesWritten += len;
            if (callback != null) {
                callback.onProgress(bytesWritten, bytesWritten); // Updated to use bytesWritten for total size
            }
        }

        public interface ProgressCallback {
            void onProgress(long bytesWritten, long totalSize);
        }
    }
}
