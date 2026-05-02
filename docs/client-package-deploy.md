# Deploying the CANIS Client Package

This project publishes the Java client as a Maven package to GitHub Packages.

Package repository:

```text
https://maven.pkg.github.com/metiago/canis-client
```

## 1. Create a GitHub Token

Create a classic GitHub personal access token with package publishing access.

Required scopes:

```text
write:packages
read:packages
```

If the package or repository is private, the token may also need:

```text
repo
```

## 2. Configure Maven Credentials

Create or update:

```text
C:\Users\TSIG\.m2\settings.xml
```

Use this template and replace `YOUR_GITHUB_USERNAME` and `YOUR_GITHUB_TOKEN`:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_TOKEN</password>
    </server>
  </servers>

  <profiles>
    <profile>
      <id>github</id>
      <repositories>
        <repository>
          <id>github</id>
          <name>GitHub metiago Apache Maven Packages</name>
          <url>https://maven.pkg.github.com/metiago/canis-client</url>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>

  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>
</settings>
```

The `<id>github</id>` entry must match the repository id in `client/pom.xml`.

## 3. Update the Client Version

Before publishing, bump the client version in:

```text
client/pom.xml
```

GitHub Packages will reject redeploying the same release version. Use a new version such as:

```xml
<version>1.0.8</version>
```

## 4. Run Tests

Run the client tests before deploying:

```powershell
mvn -q -pl client test
```

## 5. Deploy

Publish the client package:

```powershell
mvn -pl client deploy -DskipTests
```

After a successful deploy, verify the package on GitHub:

```text
https://github.com/metiago/canis-client/packages/2344030
```

## Common Failure

If Maven returns `401 Unauthorized`, check that:

- `C:\Users\TSIG\.m2\settings.xml` exists.
- The `server` id is `github`.
- The token is valid and has `write:packages`.
- The GitHub user has permission to publish to `metiago/canis-client`.
