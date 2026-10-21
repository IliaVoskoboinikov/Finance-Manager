# Security Policy

## Supported versions

Finance Manager is pre-release. Only the latest commit on `master` and the latest
[pre-release](https://github.com/IliaVoskoboinikov/Finance-Manager/releases) receive security fixes.

## Reporting a vulnerability

**Please do not open a public issue, discussion or pull request for a security problem.**

Report it privately through GitHub:
[**Report a vulnerability**](https://github.com/IliaVoskoboinikov/Finance-Manager/security/advisories/new)
(Security tab → Advisories → *Report a vulnerability*).

Please include:

- what is affected (module, file, workflow) and the impact;
- steps to reproduce or a proof of concept;
- the commit or release you tested.

You will get a reply within 7 days. Once the fix is released, the advisory is published and
the reporter is credited, unless you ask otherwise.

## Scope

In scope:

- the Android app: local data storage and encryption (`core:security`, Room, DataStore),
  PIN lock, authentication and session handling, network layer;
- the build and CI/CD pipeline: GitHub Actions workflows and composite actions, signing,
  secrets handling.

Out of scope: the test backend the pre-release build talks to, and issues that require a rooted
device or physical access to an unlocked phone.
