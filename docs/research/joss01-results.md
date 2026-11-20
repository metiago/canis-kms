# JOSS-01 research fit and observed boundary result

## Author-validated question, audience, and scope

The author identifies the research contribution as evaluating asynchronous encrypted file exchange
under a shared-trust model. The focused question tested here is: can a separately authenticated
Java service decrypt a file encrypted under a key named for another service, and how does that
behavior reflect the documented shared trust boundary? The intended audience is software engineers,
security engineers, and researchers studying service integration, key-service dependence, and
access-control trade-offs in file-based workflows.

CANIS is the software artifact examined by this research. The broader empirical study specified in
[Issues #31–35 (archived)](repository-cleanup-report.md#archived-files) will use CANIS to investigate correctness, resource use,
availability, delayed processing, and failure behavior under shared trust. That study is planned,
not yet an observed result. The executed boundary experiment below is narrower research use by the
developers. No separate external adopter or unrelated academic project is a prerequisite for
describing that use.

CANIS is an inspectable shared-trust key repository and key-recovery service built around the
internal CANISP protocol, with a Java client, file envelope, and encrypted persistence. Its role in
this study is to expose that specific trust boundary for evaluation. The author does not present it
as a competitor to Google Tink, HashiCorp Vault Transit, AWS KMS, Azure Key Vault, or Google Cloud
KMS, or claim that a new KMS is generally necessary or superior. The comparison below describes
different capabilities and access models; it is not a comparative experiment.

## Historical operational evidence and research motivation

The authors identify the KMS described in a recommendation letter dated 26 August 2024 as the
predecessor of CANIS and identify this repository as its open-source continuation. They state that
the implementation evolved while retaining the core client/server architecture, shared-trust
model, protocol concepts, and encrypted file-exchange workflow used in production. These are
author attestations of continuity, not a historical source diff or a claim of wire, database, or
file-format compatibility with the deployed predecessor. The letter is from Daniano Engers, identified
in it as a TOTVS project leader. It reports that Tiago developed a KMS to protect sensitive files
used in integrations among financial systems,
that it was deployed in production, and that it remained in use as of the letter's date. It also
reports a later open-source release and presentations inside the company and at developer events.
The author's separate account identifies Java services and legacy COBOL and PowerBuilder systems
as participants in that exchange; those languages are not named in the letter.

The local author-supplied source is `Carta Totvs Daniano.pdf` at the repository root (SHA-256
`0785D3791A2F41CA7442575EA74E261917457C0EE0D3F3CDE0AA9B445A1102B5`). It was untracked
when reviewed and has not been included as a publication artifact. The letter contains a personal
email address; permission to publish it or a redacted version has not been established. It does
not name CANIS, identify a source revision, give deployment start or end dates, or document
measured security outcomes. Its continued-use statement is bounded by 26 August 2024, not the
present date. The letter's broader claims about security incidents, standards, and awards are
not used here as verified technical or impact results. The authors permit naming TOTVS in the
manuscript but prefer the phrase "a large Brazilian enterprise software vendor" for the historical
context. That permission to name the organization does not establish permission to publish the
letter or its personal contact details.

Together, the letter and the authors' continuity statement support the historical operational
motivation and bounded production-use claim for CANIS's predecessor. The current repository's
[threat model](../threat-model.md), [CANISP guide](../canisp-protocol.md), and Java client file
helpers document those concepts in the present implementation; no historical source snapshot was
supplied to compare them directly. The evidence does not establish that the current revision was
deployed, that old file envelopes or databases work with it, or that the past deployment was a
research study.

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

## Focused rerun on 2026-11-17

From the repository root, `powershell.exe -NoProfile -ExecutionPolicy Bypass -File docs/research/run-joss01.ps1`
exited 0 on commit
`0e9c889e49b2928c2c9cd84fadaf0368d7c41c91`. The working tree had pre-existing edits to
`app-prod-grade-backlog.md` and `joss-backlog.md`; the method, runner, and test source had no
working-tree changes before execution. The test source SHA-256 was
`71FB34494FEC157CA15EA591278AA7421AD455619DDA1E36560A238F1BBB622C`.
Maven 3.9.12 used Eclipse Adoptium Java 21.0.6 and `keytool` 21.0.6 on Windows 11 x64
(`10.0.26100.0`).

The fresh `server/target/surefire-reports/TEST-io.canis.SharedTrustDomainResearchTest.xml`
was last written at `2026-11-17T16:57:04.8912385Z` and reported one test, zero failures,
zero errors, and zero skips. The test asserted byte-for-byte recovery of the synthetic input
by both `recipient` and `observer`, and rejection of the altered `observer` password. This
rerun supports the same credential-level shared-access observation described above. It used
one generated client TLS certificate across credential identities and does not establish
certificate-bound separation, general security, performance, or completion of the broader study. The
focused run does not replace a full-suite verification of a fixed candidate release.

## Comparison grounded in primary documentation

| Approach | Verified capability | Relationship to this CANIS evaluation |
| --- | --- | --- |
| [Tink KMS Envelope AEAD](https://developers.google.com/tink/client-side-encryption) | The client generates a DEK, encrypts data locally, and uses a cloud KMS to protect the DEK. | Tink supplies an envelope-encryption primitive that calls an external KMS; this experiment did not evaluate any KMS access policy configured with Tink. CANIS includes its own service with authenticated shared access. No advantage over Tink was established. |
| [Vault Transit envelope encryption](https://developer.hashicorp.com/vault/docs/secrets/transit/envelope-encryption) and [Transit overview](https://developer.hashicorp.com/vault/docs/secrets/transit) | Transit can supply wrapped DEKs for local file encryption and supports ACL restrictions on named-key operations and key rotation. | Vault can use named-key policies where narrower access is required. CANIS currently trusts admitted clients across stored keys. These are different access models; the observer result is expected under CANIS's documented model, while lack of per-key isolation remains a limitation for workflows that require it. No comparative security outcome was measured. |

The manuscript's standards citations were checked against the primary [NIST SP 800-38D](https://csrc.nist.gov/pubs/sp/800/38/d/final) record (Morris Dworkin, 2007, DOI `10.6028/NIST.SP.800-38D`) and [RFC 8017](https://www.rfc-editor.org/info/rfc8017/) (Moriarty, Kaliski, Jonsson, and Rusch, November 2016). This verifies the source identities. [JOSS-04 results (archived)](repository-cleanup-report.md#archived-files) document the later bibliography work; this JOSS-01 experiment did not build the paper.

## JOSS-01 acceptance status and research-use decision gate

| Criterion | Status | Evidence and remaining gap |
| --- | --- | --- |
| Research question, audience, and distinct purpose | Satisfied | The author identifies Issues #31–35 as the empirical study and CANIS as the artifact whose shared-trust behavior is examined. The intended audience and the reason for examining this implementation, rather than claiming superiority over Tink or Vault, are documented above. |
| Statement of need | Satisfied | The letter describes sensitive financial-system file exchange and the need for protection; the author's account supplies the Java and legacy-system context. |
| Historical operational use | Satisfied | The dated letter reports production deployment and continued use as of August 2024. The authors identify the described KMS as CANIS's predecessor and this repository as its open-source continuation; the letter itself does not name CANIS. |
| Source continuity and publication details | Partially satisfied | The authors attest continuity of architecture, trust model, protocol concepts, and workflow, while acknowledging implementation changes. No historical source snapshot or revision mapping verifies that continuity independently. The authors permit naming TOTVS but prefer a generic manuscript reference. Permission to publish or confidentially share a redacted letter remains to be confirmed. |
| Research motivation | Satisfied | The documented integration problem motivates the planned shared-trust study of CANIS; the historical production deployment was not itself documented as research. |
| Actual research use | Partially satisfied | The predefined JOSS-01 boundary experiment was executed with CANIS and has a method, revision, and result; this is limited developer research use of the artifact. Issues #31–35 are planned and cannot yet be counted as completed use. Retain the resulting data, analysis, and fixed-revision reproduction record when that study runs. External adoption or a third-party paper is not required. |
| Significance and security claims | Partially satisfied | The categorical shared-access result and documented limitations support a bounded claim about this working tree. Candidate-release verification and author approval of the final claims remain open. |
| Practical software significance | Satisfied | The letter reports production use of the predecessor, continued use as of its date, a later open-source release, and presentations. These support a bounded claim of operational relevance, not a measured security benefit for the current code. |
| JOSS research significance | Partially satisfied | The historical use and narrow boundary experiment motivate the research contribution. The broader study has no results yet, and whether the present record establishes sufficient research impact is an editorial judgment. |
| Fair comparison to existing approaches | Satisfied | The primary-source Tink and Vault comparison below states capabilities and access-model differences without claiming superiority. The other named KMS products were not evaluated. |
| Manuscript acceptance as a whole | Partially satisfied | The manuscript can state historically corroborated operational motivation and bounded JOSS-01 research use. The planned broader study has no results yet, and the final research-impact and release claims remain unverified. |

The executed JOSS-01 test is inspectable evidence that the developers used CANIS to investigate one
shared-trust question. The planned Issues #31–35 could extend that record when the experiments are
run and their results retained; a plan alone cannot do so. The current [JOSS submission rules](https://joss.readthedocs.io/en/latest/submitting.html)
allow research use by the developers themselves and do not require external adoption or a
third-party publication. They also require demonstrated research impact and say the JOSS paper
should focus on the software rather than new research results. Whether this limited self-evaluation
alone is a sufficient impact case is an editorial judgment, so JOSS-01 acceptance remains open.
Public development history is a separate submission gate.

## Limitations and next work

This is one categorical experiment with synthetic data on one Windows host. It is not a benchmark,
multi-service deployment study, or evidence of production readiness. The historical operational
account now has a locally inspectable letter, but it is untracked and does not map to this source
revision. A fixed candidate release,
full security review, independent reproduction, and the broader study results remain outstanding.
Bibliography and manuscript-build evidence are recorded separately under JOSS-04 and JOSS-05;
this JOSS-01 experiment did not compile the manuscript.
The production backlog proposes per-key authorization and stored-key validation in Issue #36;
per-key authorization would change the shared-access model and the observer result, requiring
a revised method and interpretation. The current result does not show failure of the documented
shared-access policy. No product transport, storage format,
or access policy was changed for this evaluation. The current audit logger also lacks the
structured, integrity-verifiable record required by `AGENTS.md`.
