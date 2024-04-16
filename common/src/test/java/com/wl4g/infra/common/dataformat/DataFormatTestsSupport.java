/**
 * Copyright (C) 2023 ~ 2035 the original authors WL4G (James Wong).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.infra.common.dataformat;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextDouble;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomUtils.nextBoolean;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomUtils.nextInt;

/**
 * The {@link DataFormatTestsSupport}
 *
 * @author James Wong
 * @since v1.0
 **/
public abstract class DataFormatTestsSupport {


    public static Path generateTestData0(String name) throws IOException {
        final Map<String, Object> data = new HashMap<>();

        data.put("__thingName__", "tc_S_thing_instance_name_1001");
        data.put("__deviceId__", "tc_S_thing_id_1002");
        data.put("__modelType__", "device");
        data.put("__logicalInterfaceId__", "tc_S_modelId_1003");
        data.put("__metricsType__", "1004");
        data.put("__tenantId__", "s_t_connect");
        data.put("__calculate_time__", "1713253721002");
        data.put("__assetId__", "tc_S_thing_id_1002");
        data.put("__physicalInterfaceId__", "tc_S_modelId_1003");
        data.put("__deviceTypeId__", "tc_S_modelId_1003");
        data.put("__deptScope__", "default");
        data.put("__timestamp__", "1713253721002");
        data.put("__cloud_time__", "1713253721002");

        final Path generateFile = Files.createTempFile(name, ".json");
        out.printf("Generating test file: %s%n", generateFile.toAbsolutePath());
        return Files.write(generateFile, toJSONString(data).getBytes(Charsets.UTF_8));
    }

    public static Path generateTestData1(String name) throws IOException {
        final Map<String, Object> data = new HashMap<>();

        data.put("__thingName__", "tc_S_thing_instance_name_" + nextInt(0, 10000));
        data.put("__deviceId__", "tc_S_thing_id_" + nextInt(0, 10000));
        data.put("__modelType__", "device");
        data.put("__logicalInterfaceId__", "tc_S_modelId_" + nextInt(0, 10000));
        data.put("__metricsType__", nextInt(0, 2));
        data.put("__tenantId__", "s_t_connect");
        data.put("__calculate_time__", currentTimeMillis());
        data.put("__assetId__", "tc_S_thing_id_" + nextInt(0, 10000));
        data.put("__physicalInterfaceId__", "tc_S_modelId_" + nextInt(0, 10000));
        data.put("__deviceTypeId__", "tc_S_modelId_" + nextInt(0, 10000));
        data.put("__deptScope__", "");
        data.put("__timestamp__", currentTimeMillis());
        data.put("__cloud_time__", currentTimeMillis());
        Map<String, Object> online = new HashMap<>();
        online.put("connected", nextBoolean());
        online.put("directlyLinked", nextBoolean());
        data.put("__online__", online);

        final Path generateFile = Files.createTempFile(name, ".json");
        out.printf("Generating test file: %s%n", generateFile.toAbsolutePath());
        return Files.write(generateFile, toJSONString(data).getBytes(Charsets.UTF_8));
    }

    public static Path generateTestData2(String name) throws IOException {
        final Map<String, Object> data = new HashMap<>();

        data.put("Carrier_Engine_Speed_Complement_1bhdQyk8k5W_dwd", nextDouble(0, 1000));
        data.put("unreported_warn_from", nextInt(0, 100));
        data.put("__logicalInterfaceId__", randomAlphabetic(13));
        data.put("East_West_Longitude_State", nextInt(0, 2));
        data.put("Second_Boom_Length", nextDouble(0, 10));
        data.put("c18ff6431_0016", nextInt(0, 10000));
        data.put("Actual_Lifting_Capacity", nextInt(0, 10));
        data.put("long_work_threshold", nextInt(0, 2000));
        data.put("__cloud_time__", currentTimeMillis());
        data.put("speed_alarm_from", nextInt(0, 100));
        data.put("hand_brake_state_1bhdQyk8k5W_dwd", 1);
        data.put("chaiss_worktime_gps_1bhdQyk8k5W_dwd", nextDouble(0, 10000));
        data.put("c18ff0031_0032", nextDouble(0, 10000));
        data.put("Superstructure_Power_Takeoff_Signal_1bhdQyk8k5W_dwd", 1);
        data.put("time_local", "2024-04-11 09:26:57");

        // oil_cost_gps_Context
        Map<String, Object> oilCostGpsContext = new HashMap<>();
        oilCostGpsContext.put("This_Time", currentTimeMillis());
        oilCostGpsContext.put("Last_Time", currentTimeMillis());
        oilCostGpsContext.put("Last_Value", nextDouble(0, 10000));
        oilCostGpsContext.put("Value_Abnormal_Flag", 0);
        oilCostGpsContext.put("This_Value", nextDouble(0, 10000));
        oilCostGpsContext.put("Value_Decrease_Flag", 0);
        oilCostGpsContext.put("Inc", 0);
        data.put("oil_cost_gps_Context", oilCostGpsContext);

        data.put("version", 1);
        data.put("in_Factory_Save_All_1bhdQyk8k5W_dwd", 0);
        data.put("stopCarStartTime", 0);
        data.put("has_alarm_gps", 0);
        data.put("working_duration", "aaaa");
        data.put("sit_count", 0);
        data.put("stop_car_threshold", nextDouble(0, 10000));
        data.put("crane_worktime_gps", nextDouble(0, 10000));
        data.put("Carrier_Engine_Low_Fuel_Alarm", 0);
        data.put("latitude", nextDouble(0, 100));
        data.put("chaiss_worktime", 2444.1);
        data.put("Actuator_Engine_Work_Time_Inc", 0);
        data.put("Actuator_Work_Time_Inc", 0);
        data.put("Working_Condition_Code", 0);

        // __raw_loc__
        Map<String, String> rawLoc = new HashMap<>();
        rawLoc.put("gps", "$GPRMC,,A,2755.869955763754,N,11445.651983699589,E,0.0,0.0,,,,D*38");
        data.put("__raw_loc__", rawLoc);

        // hoisting_count_gps_Context
        Map<String, Object> hoistingCountGpsContext = new HashMap<>();
        hoistingCountGpsContext.put("This_Time", currentTimeMillis());
        hoistingCountGpsContext.put("Last_Time", currentTimeMillis());
        hoistingCountGpsContext.put("Last_Value", nextDouble(0, 10000));
        hoistingCountGpsContext.put("Value_Abnormal_Flag", 0);
        hoistingCountGpsContext.put("This_Value", nextDouble(0, 10000));
        hoistingCountGpsContext.put("Value_Decrease_Flag", 0);
        hoistingCountGpsContext.put("Inc", 0);
        data.put("hoisting_count_gps_Context", hoistingCountGpsContext);

        // longWorkAlarmState
        Map<String, Object> longWorkAlarmState = new HashMap<>();
        longWorkAlarmState.put("duration", 80000);
        longWorkAlarmState.put("isOverspeedAlarm", false);
        longWorkAlarmState.put("currentTimestamp", currentTimeMillis());
        longWorkAlarmState.put("lastAlarmTs", currentTimeMillis());
        longWorkAlarmState.put("endTs", currentTimeMillis());
        longWorkAlarmState.put("startTs", currentTimeMillis());
        longWorkAlarmState.put("step", nextInt(0, 10));
        longWorkAlarmState.put("threshold", nextInt(0, 10000));
        data.put("longWorkAlarmState", longWorkAlarmState);

        //data.clear(); // for test: complex json structures for human readability.

        // carStateNew
        Map<String, Object> carStateNew = new HashMap<>();
        carStateNew.put("currentT", 1712798817000L);
        carStateNew.put("actualLiftingCapacitySet", Arrays.asList(
                nextDouble(1, 100), nextDouble(1, 100), nextDouble(1, 100),
                nextDouble(1, 100), nextDouble(1, 100)));
        carStateNew.put("liftingHeight", Arrays.asList(
                nextDouble(1, 100), nextDouble(1, 100), nextDouble(1, 100),
                nextDouble(1, 100), nextDouble(1, 100), nextDouble(1, 100),
                nextDouble(1, 100), nextDouble(1, 100), nextDouble(1, 100)));
        data.put("carStateNew", carStateNew);

        final Path generateFile = Files.createTempFile(name, ".json");
        out.printf("Generating test file: %s%n", generateFile.toAbsolutePath());
        return Files.write(generateFile, toJSONString(data).getBytes(Charsets.UTF_8));
    }

    @SuppressWarnings("unused")
    public static String loadResourceFileToString(String filename) {
        return testDataCache.computeIfAbsent(filename, f -> {
            try {
                final String resourceName = String.format("dataformat/%s", filename);
                final URI uri = Resources.getResource(resourceName).toURI();
                return Resources.toString(uri.toURL(), Charsets.UTF_8);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private static final Map<String, String> testDataCache = new ConcurrentHashMap<>(2);

}
