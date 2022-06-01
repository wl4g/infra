/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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
package com.wl4g.infra.common.cli;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.infra.common.lang.Assert2.isTrue;
import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.findField;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.findMethod;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.getField;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.invokeMethod;
import static com.wl4g.infra.common.reflect.ReflectionUtils2.makeAccessible;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;

import com.wl4g.infra.common.lang.Assert2;

import lombok.AllArgsConstructor;

/**
 * Command utility.
 * 
 * @author wanglsir@gmail.com, 983708408@qq.com
 * @version 2019年12月29日 v1.0.0
 * @see
 */
public class CommandLineTool {

    /**
     * New create builder. {@link Builder}
     * 
     * @return
     */
    public final static Builder builder() {
        return new Builder();
    }

    /**
     * Command line builder tool.
     * 
     * @author Wangl.sir
     * @version v1.0.0 2019-09-08
     * @since
     */
    public final static class Builder {
        protected final Logger log = getLogger(getClass());

        private final RemovableOptions options = new RemovableOptions();

        /**
         * Adds option to options.
         * 
         * @param opt
         *            Short option.
         * @param longOpt
         *            Long option.
         * @param defaultValue
         *            Null means there is no default value, that is, the
         *            parameter is required
         * @param description
         * @return Argument description
         */
        public Builder option(String opt, String longOpt, String defaultValue, String description) {
            boolean required = isNull(defaultValue);
            HelpOption option = new HelpOption(opt, longOpt, defaultValue, required, description);
            option.setRequired(required);
            options.addOption(option);
            return this;
        }

        /**
         * Remove option to options.
         * 
         * @param opt
         * @param longOpt
         * @return
         */
        public Builder removeOption(String opt, String longOpt) {
            Assert2.notNull(options, "Options did not initialize creation");
            Option option = new Option(opt, longOpt, true, "");
            options.removeOption(option);
            return this;
        }

        public void printUsage(String header, String footer, boolean exit) {
            new HelpFormatter().printHelp(120, "\n", header, options, footer);
            if (exit) {
                System.exit(1);
            }
        }

        public Builder printUsageIfEmpty(String[] args) {
            if (isNull(args) || args.length == 0) {
                printUsage("", "", true);
            }
            return this;
        }

        /**
         * Build parsing to command line.
         * 
         * @param args
         * @return
         */
        public CommandLineWrapper build(String args[]) {
            try {
                Properties props = new Properties();
                options.getOptions().forEach(opt -> props.setProperty(opt.getOpt(), trimToEmpty(opt.getValue())));
                CommandLine line = new DefaultParser().parse(options, args, props);

                if (log.isDebugEnabled()) {
                    List<String> printArgs = options.getOptions()
                            .stream()
                            .map(o -> o.getOpt() + "|" + o.getLongOpt() + "=" + o.getValue())
                            .collect(toList());
                    log.debug("Parsed commond line args: {}", printArgs);
                }

                return new CommandLineWrapper(line, this);
            } catch (ParseException e) {
                new HelpFormatter().printHelp(120, "\n", "", options, "");
                System.exit(0);
            }
            return null;
        }

    }

    /**
     * Help option.</br>
     * 
     * @author Wangl.sir <983708408@qq.com>
     * @version v1.0 2019年5月12日
     * @since
     */
    public static class HelpOption extends Option {
        private static final long serialVersionUID = 1950613325131445963L;

        /**
         * Shell option default value.
         */
        final private String defaultValue;

        public HelpOption(String opt, String longOpt, String defaultValue, boolean required, String description)
                throws IllegalArgumentException {
            super(opt, longOpt, true, description);
            isTrue(opt.length() == 1,
                    format("Short option: '%s' (%s), non GNU specification, name length must be 1", opt, description));
            this.defaultValue = defaultValue;
            setRequired(required);
            if (!isRequired()) {
                setArgName("default=" + defaultValue);
            } else {
                setArgName("required");
            }
        }

        public String getDefaultValue() {
            return defaultValue;
        }

    }

    public static class RemovableOptions extends Options {
        private static final long serialVersionUID = -3292319664089354481L;

        /**
         * Remove an option instance
         *
         * @param opt
         *            the option that is to be added
         * @return the resulting Options instance
         */
        public RemovableOptions removeOption(Option opt) {
            if (!isNull(opt)) {
                getShortOpts().remove(opt.getOpt());
                getLongOpts().remove(opt.getLongOpt());
                getRequiredOpts().remove(opt.getOpt());
            }
            return this;
        }

        @SuppressWarnings("unchecked")
        final private Map<String, Option> getShortOpts() {
            Field field = findField(Options.class, "shortOpts");
            return (Map<String, Option>) getField(field, this);
        }

        @SuppressWarnings("unchecked")
        final private Map<String, Option> getLongOpts() {
            Field field = findField(Options.class, "longOpts");
            return (Map<String, Option>) getField(field, this);
        }

        @SuppressWarnings("unchecked")
        final private Map<String, Option> getRequiredOpts() {
            Field field = findField(Options.class, "requiredOpts");
            return (Map<String, Option>) getField(field, this);
        }
    }

    @AllArgsConstructor
    public static class CommandLineWrapper {
        private final CommandLine line;
        private final Builder builder;

        public String get(String opt) throws ParseException {
            return getString(opt);
        }

        public String getString(String opt) throws ParseException {
            return getCheckOptionValue(opt);
        }

        public Long getLong(String opt) throws ParseException {
            String value = getCheckOptionValue(opt);
            return isBlank(value) ? null : Long.parseLong(value);
        }

        public Integer getInteger(String opt) throws ParseException {
            String value = getCheckOptionValue(opt);
            return isBlank(value) ? null : Integer.parseInt(value);
        }

        public Float getFloat(String opt) throws ParseException {
            String value = getCheckOptionValue(opt);
            return isBlank(value) ? null : Float.parseFloat(value);
        }

        public Double getDouble(String opt) throws ParseException {
            String value = getCheckOptionValue(opt);
            return isBlank(value) ? null : Double.parseDouble(value);
        }

        private String getCheckOptionValue(String opt) throws ParseException {
            // Check for use opt invalid?
            if (!safeArrayToList(line.getOptions()).stream()
                    .anyMatch(o -> equalsAnyIgnoreCase(opt, o.getOpt(), o.getLongOpt()))) {
                throw new ParseException(format("\nUsing undeclared options: %s\n", opt));
            }

            String value = line.getOptionValue(opt);
            if (isBlank(value)) {
                makeAccessible(resolveOptionMethod);
                HelpOption option = (HelpOption) invokeMethod(resolveOptionMethod, line, opt);
                if (nonNull(option)) {
                    value = option.getDefaultValue();
                    if (option.isRequired() && isNull(value)) {
                        String errmsg = format("\nBad command option: '-%s,--%s' is missing. Please use: help,--help\n",
                                option.getOpt(), option.getLongOpt());
                        builder.printUsage("", errmsg, true);
                    }
                }
            }
            return value;
        }
    }

    private static final Method resolveOptionMethod = findMethod(CommandLine.class, "resolveOption", String.class);

}