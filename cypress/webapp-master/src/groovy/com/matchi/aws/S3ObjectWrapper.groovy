package com.matchi.aws

import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.S3ObjectId
import com.amazonaws.services.s3.model.S3ObjectInputStream
import com.amazonaws.util.StringUtils

class S3ObjectWrapper implements Closeable {

    private final S3Object s3obj;

    S3ObjectWrapper(S3Object s3obj) {
        if (s3obj == null) throw new IllegalArgumentException()
        this.s3obj = s3obj
    }

    private static String from(InputStream inputStream) throws IOException {
        if (inputStream == null) return ""

        StringBuilder stringBuilder = new StringBuilder()
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StringUtils.UTF8))
            String line
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line)
            }
        } finally {
            inputStream.close()
        }
        return stringBuilder.toString()
    }

    public String getBucketName() { s3obj.getBucketName() }

    public String getKey() { s3obj.getKey() }

    String toJsonString() { from(s3obj.getObjectContent()) }

    @Override
    public String toString() { return s3obj.toString() }

    @Override
    public void close() throws IOException { s3obj.close() }
}