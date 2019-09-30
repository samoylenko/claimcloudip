# Claim Cloud IP

A simple and dumb program to assign and track IPs in a specified subnet. Created
to support
[CI](https://en.wikipedia.org/wiki/Continuous_integration)/[CD](https://en.wikipedia.org/wiki/Continuous_delivery)
pipelines that require assignment and tracking of static IP addresses. An
example usage scenario is supporting multiple Git branches in a single project
that creates a number of Virtual Machines per branch so that developers can
easily test their work prior to submitting merge requests.

For example, there is a [GitLab](https://gitlab.com/) project with 3 virtual
machines that use static IPs and all share a test database, so they need to be
on the same subnet. Simply making branch/tag a key parameter will allow that
happen:

```shell
BRANCH_IPS=$(java -jar claimcloudip-<version>.jar -g 3 -k ${CI_COMMIT_REF_SLUG} -c 192.168.0.0/24)
```

## Requirements
1. Requires JRE 8
2. To keep IP allocation data between builds, ensure that configuration of this
   program (parameter `-f`) is persistent.
