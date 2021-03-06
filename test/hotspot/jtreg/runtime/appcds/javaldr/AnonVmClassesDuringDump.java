/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */

/*
 * @test
 * @summary When dumping the CDS archive, try to load VM anonymous classes to make sure they
 *          are handled properly. Note: these are not "anonymous inner classes" in the Java source code,
 *          but rather classes that are not recorded in any ClassLoaderData::dictionary(),
 *          such as classes that are generated for Lambda expressions.
 *          See https://blogs.oracle.com/jrose/anonymous-classes-in-the-vm.
 * @library /test/lib /test/hotspot/jtreg/runtime/appcds /test/hotspot/jtreg/runtime/appcds/test-classes
 * @requires vm.cds
 * @requires vm.flavor != "minimal"
 * @modules java.base/jdk.internal.misc
 *          jdk.jartool/sun.tools.jar
 *          java.management
 * @build AnonVmClassesDuringDumpTransformer Hello
 * @run main/othervm AnonVmClassesDuringDump
 */

public class AnonVmClassesDuringDump {
    public static String appClasses[] = {
        "Hello",
    };
    public static String agentClasses[] = {
        "AnonVmClassesDuringDumpTransformer",
    };

    public static void main(String[] args) throws Throwable {
        String agentJar =
            ClassFileInstaller.writeJar("AnonVmClassesDuringDumpTransformer.jar",
                                        ClassFileInstaller.Manifest.fromSourceFile("AnonVmClassesDuringDumpTransformer.mf"),
                                        agentClasses);

        String appJar =
            ClassFileInstaller.writeJar("AnonVmClassesDuringDumpApp.jar", appClasses);

        TestCommon.testDump(appJar, TestCommon.list("Hello"),
                            "-javaagent:" + agentJar,
                            // Set the following property to see logs for dynamically generated classes
                            // in STDOUT
                            "-Djava.lang.invoke.MethodHandle.DUMP_CLASS_FILES=true");
        TestCommon.run("-cp", appJar, "Hello")
            .assertNormalExit();
    }
}

