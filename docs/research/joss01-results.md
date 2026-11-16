# JOSS-01 research fit and observed boundary result

## Candidate question and audience

Question: In an asynchronous encrypted-file workflow, can a separately authenticated Java
service decrypt a file encrypted under a key named for another service, and how does that
behavior reflect the documented shared trust boundary? The intended audience is researchers
and developers studying service integration, key-service dependence, and access-control
trade-offs in file-based workflows. The repository
provides an inspectable Java client, CANISP server, file envelope, and encrypted persistence
implementation for that examination. This is a proposed research-software contribution, not a
claim that CANIS is superior to established KMS products.

## Observed evaluation

The method was recorded in [joss01-method.md](joss01-method.md) before the test was run. On
2026-11-16, `SharedTrustDomainResearchTest` passed on commit
`b8a48ba4f136dfb7a895dd9465f0fef701c5bd81` plus the uncommitted test and documentation
changes described here. The final focused run, after a deterministic invalid-password refinement,
again passed; its Surefire XML reported one test, zero failures, zero errors, and zero skips. The
SHA-256 of the final `SharedTrustDomainResearchTest.java` on this host was
`71FB34494FEC157CA15EA591278AA7421AD455619DDA1E36560A238F1BBB622C`.
Maven was 3.9.12; Maven used Eclipse Adoptium Java 21.0.6 on Windows 11 amd64. The
independent `java -version` command reported Oracle Java 21.0.7, so the Maven JDK is the relevant
runtime for this result.

The generated fixture was encrypted by `producer` under the `recipient` key over the official TLS
client. Both `recipient` and a distinct, valid `observer` credential identity recovered bytes
equal to the input. A deliberately altered password for `observer` was rejected at login. These
are the observed outcomes of one synthetic scenario.

The [documented trust model](../threat-model.md) admits clients through TLS client-certificate
validation and CANIS credentials, then permits authenticated clients to request decryption with
any stored key. Key names select cryptographic material; they do not define per-service access
rights. The observer result is consistent with that shared-access design and validates this
particular behavior of the implemented boundary. It is not evidence of a bypass of an implemented
per-key policy or an unexpected authorization failure. The security implication remains that an
admitted client able to obtain an encrypted file can request its data key even when a different
service name was used for encryption. A workflow requiring isolation among admitted services
needs an additional authorization boundary that CANIS does not currently provide. The test used
one temporary client TLS certificate for all three credential identities, so it does not evaluate
certificate-bound identity separation. It also does not establish general confidentiality,
production readiness, cryptographic strength, performance, or research adoption.

The first sandboxed Windows run failed before server startup because setting the test master-key
directory ACL was denied. The successful run used the same script outside that filesystem
sandbox; this is an execution-environment limitation, not an application result. The script also
requires process-scoped `-ExecutionPolicy Bypass` on the Windows host because local PowerShell
script execution was disabled.

`mvn -q test` then completed successfully on the same Windows host. The Maven Surefire XML reports
59 client tests (zero failures, zero errors, five skips) and 90 server tests (zero failures, zero
errors, two skips), including a passing invocation of the shared-trust test before its final
invalid-password refinement. The final focused rerun passed afterward. The skips
include platform-dependent coverage; this Windows run does not establish POSIX permission behavior.

## Comparison grounded in primary documentation

| Approach | Verified capability | Relationship to this CANIS evaluation |
| --- | --- | --- |
| [Tink KMS Envelope AEAD](https://developers.google.com/tink/client-side-encryption) | The client generates a DEK, encrypts data locally, and uses a cloud KMS to protect the DEK. | Tink supplies an envelope-encryption primitive that calls an external KMS; this experiment did not evaluate any KMS access policy configured with Tink. CANIS includes its own service with authenticated shared access. No advantage over Tink was established. |
| [Vault Transit envelope encryption](https://developer.hashicorp.com/vault/docs/secrets/transit/envelope-encryption) and [Transit overview](https://developer.hashicorp.com/vault/docs/secrets/transit) | Transit can supply wrapped DEKs for local file encryption and supports ACL restrictions on named-key operations and key rotation. | Vault can use named-key policies where narrower access is required. CANIS currently trusts admitted clients across stored keys. These are different access models; the observer result is expected under CANIS's documented model, while lack of per-key isolation remains a limitation for workflows that require it. No comparative security outcome was measured. |

The manuscript's standards citations were checked against the primary [NIST SP 800-38D](https://csrc.nist.gov/pubs/sp/800/38/d/final) record (Morris Dworkin, 2007, DOI `10.6028/NIST.SP.800-38D`) and [RFC 8017](https://www.rfc-editor.org/info/rfc8017/) (Moriarty, Kaliski, Jonsson, and Rusch, November 2016). This verifies the source identities, not the missing BibTeX entries or a successful paper build.

## Research-use decision gate

No inspectable record of CANIS being used in a research project has been supplied or found in the
repository. The motivating integration workload and this self-contained evaluation do not by
themselves prove research adoption. The authors must supply the researcher, research question or
project, use context, dates, software revision, and a shareable artifact or permitted confidential
evidence. Until then, JOSS-01 acceptance and JOSS research-impact screening remain open. Current
[JOSS submission rules](https://joss.readthedocs.io/en/latest/submitting.html) require actual
research use at least by the developers and a sufficiently long public development history;
local commit dates alone do not verify public availability.

## Limitations and next work

This is one categorical experiment with synthetic data on one Windows host. It is not a benchmark,
multi-service deployment study, or evidence of production readiness. A fixed candidate release,
full security review, independent reproduction, and a real research-use record remain outstanding.
The later JOSS-04 work added a bibliography beside the manuscript, but its
documented Inara attempt did not produce a PDF. This JOSS-01 experiment did
not compile the manuscript.
The production backlog proposes per-key authorization and stored-key validation in Issue #36;
per-key authorization would change the shared-access model and the observer result, requiring
a revised method and interpretation. The current result does not show failure of the documented
shared-access policy. No product transport, storage format,
or access policy was changed for this evaluation. The current audit logger also lacks the
structured, integrity-verifiable record required by `AGENTS.md`.
