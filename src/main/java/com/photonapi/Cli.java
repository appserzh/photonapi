package com.photonapi;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.DefaultParser;

public class Cli {

    private Options options = new Options();
    private CommandLine commandLine;

    Cli(String[] args) throws ParseException {
        options.addOption("username", true, "Real login")
                .addOption("password", true, "Real password")
                .addOption("img", true, "Image for analysis")
                .addOption("host", true, "Host and port. example - 192.168.1.1:8888");
        commandLine = (new DefaultParser()).parse(options, args);
    }

    public String getPassword() {
        return getValOrDefault("password");
    }

    public String getLogin() {
        return getValOrDefault("username");
    }

    public String getFile() {
        return getValOrDefault("img", null);
    }

    public String getHost() {
        return getValOrDefault("host", "localhost:10300");
    }

    private String getValOrDefault(String arg) {
        return getValOrDefault(arg, arg);
    }

    private String getValOrDefault(String arg, String def) {
        return commandLine.hasOption(arg) ? commandLine.getOptionValue(arg) : def;
    }
}
