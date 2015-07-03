/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spechelper;

import java.util.Comparator;

public class Test {
    
    public Test() {}
    
    public <E>  void bla(Comparable<Long> comp1, Comparator<Comparable<String>> comp2, E arg, int a, byte[] b, char[] c, double d, float f, boolean bo, int[][] ar, String[][] arr,
            long l, String... strs) {
    }
    
    public String fooaaa(int aasdfsdfsd) {
        return null;
    }
    
    private void priv() {}

    protected void prot() {}
    
    void packloc() {}
    
    public void testInputfoo() {
        Math.sin(0d);
    }
}
