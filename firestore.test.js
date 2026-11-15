const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read zones, facilities, or stores", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("zones").get());
  await assertFails(unauthDb.collection("facilities").get());
  await assertFails(unauthDb.collection("stores").get());
});

test("Authenticated user: cannot read another user's zone or appointment", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("zones").doc("bob_zone").set({
      userId: BOB_UID,
      name: "حي الشهداء",
      powerStatus: "NATIONAL",
      powerStartTime: "08:00",
      powerOffDuration: "4 ساعات",
      powerFaultReason: "لا يوجد",
      powerExpectedFix: "مستقر",
      waterStatus: "PUMPING",
      waterOpenTime: "06:00",
      waterDuration: "6 ساعات",
      waterFaultNote: "لا يوجد",
      waterExpectedReturn: "مستمر",
      coordsText: "0.2,0.2;0.4,0.2;0.4,0.4;0.2,0.4",
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("zones").doc("bob_zone").get());
});

test("Authenticated user: can query their own zones with matching userId filter", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("zones").doc("alice_zone").set({
      userId: ALICE_UID,
      name: "مركز زمار",
      powerStatus: "NATIONAL",
      powerStartTime: "09:00",
      powerOffDuration: "3 ساعات",
      powerFaultReason: "لا يوجد",
      powerExpectedFix: "مستقر",
      waterStatus: "PUMPING",
      waterOpenTime: "07:00",
      waterDuration: "5 ساعات",
      waterFaultNote: "لا يوجد",
      waterExpectedReturn: "مستمر",
      coordsText: "0.3,0.3;0.5,0.3;0.5,0.5;0.3,0.5",
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("zones").where("userId", "==", ALICE_UID).get()
  );
});

test("Authenticated user: fails query without userId filter", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("zones").get());
});

test("Shadow update test: rejects extra unauthorized fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("stores").doc("store_1").set({
      userId: ALICE_UID,
      name: "معرض زمار",
      category: "غذائية",
      description: "متجر مواد غذائية",
      address: "الشارع العام",
      approvalStatus: "APPROVED",
      mapX: 0.5,
      mapY: 0.5,
      isSuperVerifiedGhostField: true,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});
