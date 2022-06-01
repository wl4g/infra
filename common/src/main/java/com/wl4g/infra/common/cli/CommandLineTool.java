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
import static com.wl4g.infra.common.lang.Assert2.hasTextOf;
import static com.wl4g.infra.common.lang.Assert2.notNull;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
         * ADD command option with not default value(ie:required).
         * 
         * @param longOpt
         *            Long option.
         * @param help
         * @return
         */
        public Builder mustOption(@NotBlank String longOpt, @Nullable String help) {
            return mustOption(null, longOpt, help);
        }

        /**
         * ADD command option with not default value(ie:required).
         * 
         * @param shortOpt
         *            Short option.
         * @param longOpt
         *            Long option.
         * @param help
         * @return
         */
        public Builder mustOption(@Nullable String shortOpt, @NotBlank String longOpt, @Nullable String help) {
            options.addOption(new HelpOption(shortOpt, longOpt, null, true, help));
            return this;
        }

        /**
         * ADD command option with default value(ie:not required).
         * 
         * @param longOpt
         *            Long option.
         * @param defaultValue
         *            Null means there is no default value, that is, the
         *            parameter is required
         * @param help
         * @return
         */
        public Builder option(@NotBlank String longOpt, @Nullable String defaultValue, @Nullable String help) {
            return option(null, longOpt, defaultValue, help);
        }

        /**
         * ADD command option with default value(ie:not required).
         * 
         * @param shortOpt
         *            Short option.
         * @param longOpt
         *            Long option.
         * @param defaultValue
         *            Null means there is no default value, that is, the
         *            parameter is required
         * @param help
         * @return
         */
        public Builder option(
                @Nullable String shortOpt,
                @NotBlank String longOpt,
                @Nullable String defaultValue,
                @Nullable String help) {
            options.addOption(new HelpOption(shortOpt, longOpt, defaultValue, false, help));
            return this;
        }

        /**
         * Remove option from options.
         * 
         * @param opt
         * @param longOpt
         * @return
         */
        public Builder removeOption(@Nullable String shortOpt, @NotBlank String longOpt) {
            notNull(options, "Options did not initialize creation");
            Option option = new Option(shortOpt, hasTextOf(longOpt, "longOpt"), true, "");
            options.removeOption(option);
            return this;
        }

        public void help(String header, String footer, boolean exit) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setSyntaxPrefix("Usages: [OPTIONS] ...");
            formatter.printHelp(120, "\n", header, options, footer);
            if (exit) {
                System.exit(1);
            }
        }

        public Builder helpIfEmpty(String[] args, boolean exit) {
            if (isNull(args) || args.length == 0) {
                help("", "", exit);
            }
            return this;
        }

        public Builder helpIfEmpty(String[] args) {
            return helpIfEmpty(args, true);
        }

        /**
         * Build parsing to command line wrapper.
         * 
         * @param args
         * @return
         */
        public CommandLineFacade build(String args[]) {
            // If there is only arguments 'help,--help' then print usage and
            // exit.
            if (checkHelp(args)) {
                help("", "", true);
                return null;
            }

            try {
                // Parsing to Command Line.
                Properties props = new Properties();
                options.getOptions().forEach(opt -> props.setProperty(opt.getLongOpt(), trimToEmpty(opt.getValue())));
                CommandLine line = new DefaultParser().parse(options, args, props);

                // Debug arguments pre-parse logs.
                if (nonNull(System.getProperty("debug"))) {
                    List<String> printArgs = safeArrayToList(line.getOptions()).stream().map(o -> {
                        String value = o.getValue();
                        value = isBlank(value) ? ((HelpOption) o).getDefaultValue() : value;
                        return "-".concat(o.getOpt()).concat(",--").concat(o.getLongOpt()).concat("=").concat(trimToEmpty(value));
                    }).collect(toList());
                    System.out.printf("%s Pre parsed: %s\n\n", new Date().toString(), printArgs);
                }

                return new CommandLineFacade(line, this);
            } catch (ParseException e) {
                new HelpFormatter().printHelp(120, "\n", "", options, "");
                System.exit(0);
            }

            return null;
        }

        private boolean checkHelp(String args[]) {
            return isNull(args) || (args.length == 1 && equalsAnyIgnoreCase(args[0], "help", "--help"));
        }
    }

    @Getter
    public static class HelpOption extends Option {
        private static final long serialVersionUID = 1950613325131445963L;

        private final String defaultValue;

        public HelpOption(@Nullable String shortOpt, @NotBlank String longOpt, @Nullable String defaultValue, boolean required,
                @Nullable String help) throws IllegalArgumentException {
            super(shortOpt, hasTextOf(longOpt, "longOpt"), true, help);
            // isTrue(shortOpt.length()==1,format("Bad short option: '%s' (%s),
            // non
            // GNU specification, name length must be 1", shortOpt, help));
            this.defaultValue = defaultValue;
            setRequired(required);
            if (!isRequired()) {
                setArgName("default=" + defaultValue);
            } else {
                setArgName("required");
            }
        }
    }

    public static class RemovableOptions extends Options {
        private static final long serialVersionUID = -3292319664089354481L;

        /**
         * Remove an option instance
         *
         * @param option
         *            the option that is to be added
         * @return the resulting Options instance
         */
        public RemovableOptions removeOption(@Nullable Option option) {
            if (!isNull(option)) {
                getShortOpts().remove(option.getOpt());
                getLongOpts().remove(option.getLongOpt());
                getRequiredOpts().remove(option.getOpt());
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
    public static class CommandLineFacade {
        private final CommandLine line;
        private final Builder builder;

        public String get(@NotBlank String opt) throws ParseException {
            return getString(opt);
        }

        public String getString(@NotBlank String opt) throws ParseException {
            return getCheckOptionValue(opt);
        }

        public Long getLong(@NotBlank String opt) throws ParseException {
            String value = getCheckOptionValue(opt);
            return isBlank(value) ? null : Long.parseLong(value);
        }

        public Integer getInteger(@NotBlank String opt) throws ParseException {
            String value = getCheckOptionValue(opt);
            return isBlank(value) ? null : Integer.parseInt(value);
        }

        public Float getFloat(@NotBlank String opt) throws ParseException {
            String value = getCheckOptionValue(opt);
            return isBlank(value) ? null : Float.parseFloat(value);
        }

        public Double getDouble(@NotBlank String opt) throws ParseException {
            String value = getCheckOptionValue(opt);
            return isBlank(value) ? null : Double.parseDouble(value);
        }

        public Boolean getBoolean(@NotBlank String opt) throws ParseException {
            String value = getCheckOptionValue(opt);
            return isBlank(value) ? null : Boolean.parseBoolean(value);
        }

        public <E extends Enum<?>> E getEnum(@NotBlank String opt, @NotNull Class<E> enumClass) throws ParseException {
            String value = getCheckOptionValue(opt);
            return isBlank(value) ? null
                    : safeArrayToList(enumClass.getEnumConstants()).stream()
                            .filter(e -> equalsAnyIgnoreCase(value, e.name()))
                            .findFirst()
                            .orElse(null);
        }

        private String getCheckOptionValue(String opt) throws ParseException {
            hasTextOf(opt, "opt");

            // Check for use opt invalid?
            if (!safeArrayToList(line.getOptions()).stream()
                    .anyMatch(o -> equalsAnyIgnoreCase(opt, o.getOpt(), o.getLongOpt()))) {
                throw new ParseException(format("\nUsing undeclared options: %s\n", opt));
            }

            // Gets argument values from line and default value in turn.
            String value = line.getOptionValue(opt);
            if (isBlank(value)) {
                makeAccessible(resolveOptionMethod);
                HelpOption option = (HelpOption) invokeMethod(resolveOptionMethod, line, opt);
                if (nonNull(option)) {
                    value = option.getDefaultValue();
                    if (option.isRequired() && isNull(value)) {
                        String errmsg = format("\nBad command option: '-%s,--%s' is missing. Please use: help,--help\n",
                                option.getOpt(), option.getLongOpt());
                        builder.help("", errmsg, true);
                    }
                }
            }
            return value;
        }
    }

    private static final Method resolveOptionMethod = findMethod(CommandLine.class, "resolveOption", String.class);

}