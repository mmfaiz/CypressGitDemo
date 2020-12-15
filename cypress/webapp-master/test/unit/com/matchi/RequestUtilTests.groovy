package com.matchi

import org.junit.Test

import static com.matchi.RequestUtil.*

/**
 * @author Sergei Shushkevich
 */
class RequestUtilTests {

    @Test
    void testToListFromString() {
        assert 0 == toListFromString("")?.size()
        assert 0 == toListFromString(null)?.size()

        def result = toListFromString("a,b")
        assert 2 == result.size()
        assert "a" == result[0]
        assert "b" == result[1]

        result = toListFromString("[a,b]")
        assert 2 == result.size()
        assert "a" == result[0]
        assert "b" == result[1]
    }
}
