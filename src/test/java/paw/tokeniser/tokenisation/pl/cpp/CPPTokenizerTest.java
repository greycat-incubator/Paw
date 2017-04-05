/**
 * Copyright 2017 Matthieu Jimenez.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package paw.tokeniser.tokenisation.pl.cpp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class CPPTokenizerTest {
    private final static String c_prog = "#include <iostream>\n" +
            "#include <iomanip>\n" +
            "#include \"Time.h\"\n" +
            "using namespace std;\n" +
            " \n" +
            "Time :: Time(const int h, const int m, const int s) \n" +
            "  : hour(h), minute (m), second(s)\n" +
            "{}\n" +
            " \n" +
            "void Time :: setTime(const int h, const int m, const int s) \n" +
            "{\n" +
            "     hour = h;\n" +
            "     minute = m;\n" +
            "     second = s;     \n" +
            "}\t\t\n" +
            " \n" +
            "void Time :: print() const\n" +
            "{\n" +
            "     cout << setw(2) << setfill('0') << hour << \":\"\n" +
            "\t<< setw(2) << setfill('0') << minute << \":\"\n" +
            " \t<< setw(2) << setfill('0') << second << \"\\n\";\t\n" +
            " \n" +
            "}\n" +
            " \n" +
            "bool Time :: equals(const Time &otherTime)\n" +
            "{\n" +
            "     if(hour == otherTime.hour \n" +
            "          && minute == otherTime.minute \n" +
            "          && second == otherTime.second)\n" +
            "          return true;\n" +
            "     else\n" +
            "          return false;\n" +
            "}";
    private final static String not_c_prog = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
    private final static String h_file = "#ifndef TIME_H\n" +
            "#define TIME_H\n" +
            "\n" +
            "class Time\n" +
            "{\n" +
            "     private :\n" +
            "          int hour;\n" +
            "          int minute;\n" +
            "          int second;\n" +
            "     public :\n" +
            "          //with default value\n" +
            "          Time(const int h = 0, const int m  = 0, const int s = 0);\n" +
            "          //\tsetter function\n" +
            "          void setTime(const int h, const int m, const int s);\n" +
            "          // Print a description of object in \" hh:mm:ss\"\n" +
            "          void print() const;\n" +
            "          //compare two time object\n" +
            "          bool equals(const Time&);\n" +
            "};\n" +
            " \n" +
            "#endif";
    private final static String empty = "";
    private CPPTokenizer tokenizer;

    @BeforeEach
    void buildTokenizer() {
        this.tokenizer = new CPPTokenizer();
    }

    @Test
    void cProg() {
        String[] result = tokenizer.tokenize(c_prog);
        System.out.println(Arrays.toString(result));
    }


    @Test
    void notCProg() {
        String[] result = tokenizer.tokenize(not_c_prog);
        System.out.println(Arrays.toString(result));
    }

    @Test
    void hFile() {
        String[] result = tokenizer.tokenize(h_file);
        System.out.println(Arrays.toString(result));
    }

    @Test
    void empty() {
        String[] result = tokenizer.tokenize(empty);
        System.out.println(Arrays.toString(result));
    }
}