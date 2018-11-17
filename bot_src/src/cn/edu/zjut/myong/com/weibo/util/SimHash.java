package cn.edu.zjut.myong.com.weibo.util;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.nio.charset.Charset;
import java.util.List;

public class SimHash {

    public static long simHash64(List<String> tokens) {
        long result = 0;
        int[] bitVector = new int[64];
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            long hash = hash64(token);
            for (int i = 0; i < bitVector.length; i++) {
                bitVector[i] += (hash & 1) == 1 ? 1 : -1;
                hash = hash >> 1;
            }
        }
        for (int aBitVector : bitVector) {
            result = result << 1;
            if (aBitVector > 0) {
                result += 1;
            }
        }
        return result;
    }

    public static int hammingDistance(long a, long b) {
        int dist = 0;
        a = a ^ b;
        while (a != 0) {
            a &= a - 1;
            dist++;
        }
        return dist;
    }

    public static double similarity(long a, long b) {
        return 1 - hammingDistance(a, b) / 64.0;
    }

    public static long hash64(String s) {
        //noinspection UnstableApiUsage
        HashFunction hashFunction = Hashing.goodFastHash(64);
        return hashFunction.newHasher().putString(s, Charset.forName("UTF-8")).hash().asLong();
    }
}
