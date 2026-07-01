# cloud-itonami-isco-5120

Open Occupation Blueprint for **ISCO-08 5120**: Cooks.

This repository designs a forkable OSS business for an independent cook: a prep-support robot performs portioning and temperature monitoring under a governor-gated actor, so the practice keeps its own food-safety and order records instead of renting a closed kitchen-management SaaS.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a prep-support robot performs ingredient portioning, temperature monitoring and cleanup tasks under an actor that proposes
actions and an independent **Culinary Governor** that gates them. The governor never
dispatches hardware itself; `:high`/`:safety-critical` actions (such as
operating near open flame or hot surfaces, or allergen cross-contact handling) require human sign-off.

A live sample of the operator console (robotics safety console, shared template) is rendered in [docs/samples/operator-console.html](docs/samples/operator-console.html) — pure-data HTML output of `kotoba.robotics.ui`.

## Core Contract

```text
menu plan + food-safety checklist + order ticket
        |
        v
Culinary Advisor -> Culinary Governor -> cook-support/plate, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, suppress
an operating record, or disclose sensitive data without governor approval and
audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `5120`). Required capabilities:

- :robotics
- :forms
- :telemetry
- :audit-ledger
- :bpmn

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
