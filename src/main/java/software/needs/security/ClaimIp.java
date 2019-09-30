package software.needs.security;

import com.google.gson.Gson;
import org.apache.commons.cli.*;
import org.apache.commons.net.util.SubnetUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ClaimIp {
    public static void main(String[] args) {
        Options options = new Options();

        options.addOption("c", "cidr", true, "CIDR, e.g. 192.168.0.0/24. Required.");
        options.addOption("k", "key", true, "Key (any string) - IP addresses are allocated/released using this value.");
        options.addOption("g", "get", true, "Get IP addresses and reserve them. " +
                "Specify amount as argument (default is 1). Requires -k/--key option");
        options.addOption("r", "release", false, "Release all IPs allocated to the specified key within one CIDR. " +
                "Requires -k/--key option");
        options.addOption("l", "list", false, "Show list of all current allocations");
        options.addOption("f", "file", true, "Database file location (JSON). Defaults to " +
                "\"claimip.json\" in the current working directory");

        if (args.length < 1) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("parameters", options);
            System.exit(-1);
        }

        Gson gson = new Gson();
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            String filename = "claimip.json";
            String key = cmd.getOptionValue("k");

            if (cmd.hasOption("f") && cmd.getOptionValue("f") != null) {
                filename = cmd.getOptionValue("f");
            }

            Path path = Paths.get(filename);
            Map<String, Map<String, String>> mappingData = Files.exists(path) ? gson.fromJson(Files.newBufferedReader(path), HashMap.class) : new HashMap<>();

            if (cmd.hasOption("l")) {
                System.out.println(gson.toJson(mappingData));
            } else {
                String cidr = cmd.getOptionValue("c");
                Map<String, String> currentCidr = mappingData.getOrDefault(cidr, new HashMap<>());

                if (cmd.hasOption("g")) {
                    SubnetUtils subnetUtils = new SubnetUtils(cidr);
                    String[] validAddressList = subnetUtils.getInfo().getAllAddresses();

                    int count = cmd.getOptionValue("g") == null ? 1 : Integer.decode(cmd.getOptionValue("g"));

                    List<String> returnedIps = new LinkedList<>();

                    for (final Map.Entry<String, String> reservedIpAddr : currentCidr.entrySet()) {
                        if (reservedIpAddr.getValue().equals(key)) {
                            returnedIps.add(reservedIpAddr.getKey());
                            if (--count < 1) break;
                        }
                    }

                    for (final String validIpAddr : validAddressList) {
                        if (!currentCidr.containsKey(validIpAddr) && count-- > 0) {
                            returnedIps.add(validIpAddr);
                            currentCidr.put(validIpAddr, key);
                        }
                    }

                    System.out.println(gson.toJson(returnedIps));
                } else if (cmd.hasOption("r")) {
                    currentCidr.values().removeIf(value -> value.equals(key));
                    System.out.println("Successfully released IP addresses");

                }

                mappingData.put(cidr, currentCidr);
                Files.write(path, gson.toJson(mappingData).getBytes());
            }

        } catch (ParseException | IOException e) {
            System.err.printf("Exception: %s%n", e.getMessage());
            System.exit(1);
        }
    }
}
