import * as mineflayer from "mineflayer";
import * as mcData from "minecraft-data";
import * as chunk from "prismarine-chunk";

import { Movements, goals, Pathfinder, pathfinder } from "mineflayer-pathfinder";
import { Vec3 } from "vec3";
import { Bot, ChatMessage } from "mineflayer";

// The default 25565 port is not used since it could cause conflicts
const SERVER_PORT = 36676;

// Timeouts are placed on many operations to avoid pointless waiting for something that isn't
// going to happen. 15 seconds is a good timeout for an operation which should complete fairly
// quickly
const SMALL_OPERATION_TIMEOUT = 15 * 1000;

// We'll also set a global timeout of 15 minutes for the entire bot's runtime incase something
// hangs which shouldn't
setTimeout(onTimeout, 15 * 60 * 1000);

// These are used everywhere so are made global, even if it is a bit dirty. They can't be
// initialized in place since there is no top level async
let bot: Bot = null;
let botMovements: Movements = null;

// Asynchronously starts the bot, any error fails the script as one might expect
start().catch(err => {
  log("Unhandled error while starting:", err);
  fail();
});

async function start() {
  // Create a bot connected to the server
  bot = await createBot();
  // Needed for navigation
  botMovements = new Movements(bot, mcData(bot.version));

  // Load the pathfinding plugin which will allow us to perform smart automatic navigation
  bot.loadPlugin(pathfinder);

  // Set up events to be fired when certain things are sent to the bot in chat. This allows us
  // to listen for certain conditions
  bot.chatAddPattern(/Joined green team/, "joinGreen");
  bot.chatAddPattern(/Replaced a slot on/, "replaceSlot");
  bot.chatAddPattern(/Closing arena/, "arenaClose");

  // Expect to be spawned within 1 minute. When we are, fire the onConnect handler. Async code
  // is not supported in listeners so any errors must be caught manually
  once("spawn", () => onConnect().catch(onError), 60 * 1000);

  // Log any message the bot receives
  on("message", onMessage);

  // If the bot leaves the server for any reason immediately fail the test
  on("end", onDisconnect);
  // If the bot takes any damage, immediately fail the test
  on("health", onHealthChange);
  // If any internal error occurs, immediately fail the test
  on("error", onError);

  // Fix for https://github.com/PrismarineJS/mineflayer/issues/1564. When the server sends
  // Mineflayer a chunk containing just air, it is misinterpreted as a request to unload said
  // chunk. This listener replaces *every* unloaded chunk as a loaded one filled with air. It is
  // worth noting that this most likely creates a memory leak, but that is acceptable for our use
  // case
  on("chunkColumnUnload", corner => {
    // Corner is the minimal block location in the chunk, so we need to convert it to chunk
    // coordinates
    const x = corner.x / 16;
    const z = corner.z / 16;

    // Load the chunk module for our version and create an empty chunk
    const Chunk = chunk(bot.version);
    const column = new Chunk();

    // Store the empty chunk back and fire the required event
    bot.world.setColumn(x, z, column);
    (bot as any).emit("chunkColumnLoad", corner);
  });
}

// Attempts to connect a bot to the server. This operation is retried until it succeeds
function createBot(): Promise<Bot> {
  // Wait at most three minutes for a successful connection to the server
  const successTimeout = setTimeout(onTimeout, 3 * 60 * 1000);

  // Recursively called promise constructor callback
  function impl(resolve, reject) {
    log("Attempting to initialize bot")

    const bot = mineflayer.createBot({
      host: "localhost",
      port: SERVER_PORT,
      // This is an illegal name, so its guaranteed not to conflict with anything
      username: "integration-bot",
    });

    // If the connection is successful, we can resolve the promise
    (bot as any).once("login", () => {
      log("Initialization successful");
      clearTimeout(successTimeout);
      resolve(bot);
    });

    // If the bot encounters an error logging in, try again later
    (bot as any).once("error", () => {
      log("Initialization failed, retrying in 5000ms");
      setTimeout(() => impl(resolve, reject), 5000);
    });
  }

  return new Promise(impl);
}

// Returns a promise that resolves after a set number of server ticks
async function delayTicks(ticks: number): Promise<void> {
  while (ticks > 0) {
    // If a tick takes more than a second, fail immediately to void getting stuck
    await uponEvent("physicTick", 1000);
    ticks--;
  }
}

// Returns a promise that resolves after an event occurs, returning any arguments passed with
// the event. If a timeout is specified and the event does not occur within the timeout, the test
// is failed
function uponEvent(event: string, timeout?: number): Promise<any[]> {
  return new Promise(resolve => once(event, (...args: any[]) => resolve(args), timeout));
}

// Fires a callback the first time an event occurs. If a timeout is specified and the event does
// not occur within the timeout, the test is failed
function once(event: string, callback: (...args: any[]) => void, timeout?: number) {
  const timeoutHandle = timeout ? setTimeout(onTimeout, timeout) : null;

  (bot as any).once(event, (...args) => {
    if (timeoutHandle) {
      clearTimeout(timeoutHandle);
    }

    callback(...args);
  });
}

// Fires a callback every time an event occurs
function on(event: string, callback: (...args: any[]) => void) {
  (bot as any).on(event, callback);
}

// Navigates the bot to a goal, returning a promise which is resolved when the bot has reached
// its destination. An optional timeout is accepted
function navigate(goal: goals.Goal, timeout?: number): Promise<void> {
  const pathfinder = (bot as any).pathfinder as Pathfinder;
  pathfinder.setMovements(botMovements);
  pathfinder.setGoal(goal);

  return uponEvent("goal_reached", timeout).then(() => {});
}

// Handles performing the tests once the bot has successfully logged into the server
async function onConnect() {
  log("Successfully connected");

  log("Waiting for chunks")
  //await bot.waitForChunksToLoad(log);
  log("Chunks loaded")

  await performExplosiveExperiment();
  await performRidingExperiment();

  succeed();
}

// Sets up for an experiment by joining the green team in the vanilla arena and obtaining a stack
// of every missile
async function setupExperiment() {
  log("Joining arena");
  bot.chat("/arena vanilla");
  await uponEvent("spawn", SMALL_OPERATION_TIMEOUT);

  log("Waiting for chunks")
  //await bot.waitForChunksToLoad(log);
  log("Chunks loaded")

  log("Joining green team");
  bot.chat("/green");
  await uponEvent("joinGreen", SMALL_OPERATION_TIMEOUT);

  // Check that we have indeed spawned in the correct location
  const atExpectedSpawnLocation = bot.entity.position.distanceTo(new Vec3(123.5, 77, 65.5)) < 1;
  if (!atExpectedSpawnLocation) {
    log("Did not spawn in the expected location:", bot.entity.position);
    fail();
  }

  log("Obtaining guardians");
  bot.chat("/replaceitem entity @p container.0 minecraft:guardian_spawn_egg 64");
  await uponEvent("replaceSlot", SMALL_OPERATION_TIMEOUT);

  log("Obtaining juggernauts");
  bot.chat("/replaceitem entity @p container.1 minecraft:ghast_spawn_egg 64");
  await uponEvent("replaceSlot", SMALL_OPERATION_TIMEOUT);

  log("Obtaining lightnings");
  bot.chat("/replaceitem entity @p container.2 minecraft:ocelot_spawn_egg 64");
  await uponEvent("replaceSlot", SMALL_OPERATION_TIMEOUT);

  log("Obtaining shieldbusters");
  bot.chat("/replaceitem entity @p container.3 minecraft:witch_spawn_egg 64");
  await uponEvent("replaceSlot", SMALL_OPERATION_TIMEOUT);

  log("Obtaining tomahawks");
  bot.chat("/replaceitem entity @p container.4 minecraft:creeper_spawn_egg 64");
  await uponEvent("replaceSlot", SMALL_OPERATION_TIMEOUT);

  // Can cause issues if the bot starts doing things immediately
  log("Waiting for world to materialize")
  await delayTicks(20);

  // Check ping, tps and mspt
  sendStatusCommands();

  // Make sure we are in the correct game mode
  if (bot.game.gameMode !== "survival") {
    log("Gamemode was not survival");
  }
}

// Runs the explosive experiment in which the bot continously places missiles along the green
// base and waits for the red base to be destroyed
async function performExplosiveExperiment() {
  log("Initiating explosive experiment")
  await setupExperiment();

  // The X coordinates furthest left and right positions on the green base
  const farLeftX = 97;
  const farRightX = 149;

  // The Y and Z coordinates of all the positions at the front of the green base
  const y = 77;
  const z = 51;

  // Start at the left front of the base with the first hotbar slot
  let x = farLeftX;
  let slot = 0;

  // The game ends when the bot is put into spectator mode
  while (bot.game.gameMode === "survival") {
    // Go to the location the missile needs to be placed
    log(`Moving to ${x} ${y} ${z}`);
    await navigate(new goals.GoalBlock(x + 0.5, y, z + 0.5), 15 * 1000);
    // Look directly down for placing a missile
    await bot.look(0, -Math.PI / 2);

    // Equip the desired missile
    bot.setQuickBarSlot(slot);
    const item = bot.heldItem?.name;
    log(`Placing ${item} from slot ${slot}`);
    // Place the missile
    bot.activateItem();

    // Move right four blocks, the minimum amount of space between missiles so they don't collide
    x += 4;
    // Move to the next slot, or back to the first if we've used all 5
    slot = (slot + 1) % 5;

    // If the next location would be past the furthest possible point...
    if (x > farRightX) {
      // ...we'll reset to the starting point
      x = farLeftX;
      // ...query the ping, tps and mspt
      sendStatusCommands();
      // ...and wait 30 seconds to avoid too much missile spam
      await delayTicks(30 * 20);
    }
  }

  // Make sure the arena closes after the game ending. We'll give it 30 seconds to do so (should
  // take 10 without lag)
  await uponEvent("arenaClose", 30 * 1000);
  log("Finished explosive experiment");
}

// Runs the riding experiment in which the bot rides from the green base to the red base on a
// tomahawk, goes to the red base spawn and then ends the game by double placing
async function performRidingExperiment() {
  log("Initiating riding experiment");
  await setupExperiment();

  // Move to the 5th block back on the base and look down in preperation for placing the
  // tomahawk. We're using the trick where the tomahawk is stuck inside the base and is then
  // restarted by breaking a glass block in order to more easily position the bot
  log("Moving to placement location");
  await navigate(new goals.GoalBlock(123.5, 77, 55.5), 15 * 1000);
  await bot.look(0, -Math.PI / 2);

  // Switch to a tomahawk and place it. The tomahawk should become stuck in the base after this
  // operation
  log("Placing tomahawk");
  bot.setQuickBarSlot(4);
  bot.activateItem();

  // Wait a second for the server to process the request and for the tomahawk to appear since
  // we'll be doing some pathfinding
  log("Waiting for missile to materialize");
  await delayTicks(20);

  // Navigate to the riding position, which is along the strip of terracotta on the left side of
  // the missile
  log("Moving to riding position");
  await navigate(new goals.GoalBlock(122.5, 74, 46.5), 15 * 1000);

  // Remove the TNT block directly in front of the terracotta strip to prevent the missile
  // exploding when it reaches the red base
  log("Defusing missile");
  const tntBlock = bot.blockAt(new Vec3(122, 73, 44));
  await bot.dig(tntBlock);

  // Break the glass block next to the back piston on the tomahawk, causing the missile to start
  // moving
  log("Starting missile");
  const glassBlock = bot.blockAt(new Vec3(122, 72, 50));
  await bot.dig(glassBlock);

  // The blocks the missile moves every tick
  const blocksPerTick = 0.08335;

  // The number of ticks which have passed since the missile began moving
  let tick = 0;
  // A callback incrementing the tick counter which we will register immediately and remove later
  const onTick = () => tick++;
  on("physicTick", onTick);

  log("Riding missile");
  // Look directly forward towards the red base so the bot moves in the correct direction
  await bot.look(0, 0, true);

  // Ride the missile for 1090 ticks, which is the duration of the trip
  while (tick < 1080) {
    // Calculate the position the tomahawk should be based on how many ticks have passed
    const targetZ = 45 - tick * blocksPerTick;
    // Go to the desired position on the tomahawk
    await navigate(new goals.GoalBlock(122.5, 74, targetZ));
  }

  // Remove the tick listener since it is no longer required
  (bot as any).removeListener("tick", onTick);

  // Navigate to the spawn location on the enemy base
  log("Moving to enemy spawn");
  await navigate(new goals.GoalBlock(123.5, 77, -64.5), 30 * 1000);

  // Move back to the front of the stopped tomahawk and look directly forwards in order to place
  // the winning missiles
  log("Moving to placement location");
  await navigate(new goals.GoalBlock(123.5, 74, -49.5), 30 * 1000);
  await bot.look(0, 0);

  // Wait a few ticks for synchronization, switch to a juggernaut and then place it
  log("Placing juggernaut");
  await delayTicks(5);
  bot.setQuickBarSlot(1);
  bot.activateItem();

  // Wait a few ticks for synchronization, switch to a shieldbuster and then place it
  log("Placing shieldbuster");
  await delayTicks(5);
  bot.setQuickBarSlot(3);
  bot.activateItem();

  // Enter spectator mode to avoid taking damage
  bot.chat("/sp");

  // Wait for the winner title to be shown
  log("Waiting for game to end");
  await uponEvent("title", 30 * 1000);

  log("Finished riding experiment");
}

// Queries the bot's ping to the server, the current mspt and the tps
function sendStatusCommands() {
  bot.chat("/ping");
  bot.chat("/mspt");
  bot.chat("/tps");
}

// Handles receiving a message by printing it to the console
function onMessage(message: ChatMessage) {
  log("Message from server:", message.toString())
}

// Handles a change in health by immediately failing the test
function onHealthChange() {
  // This is also fired when food changes, so we'll make sure its not a false alarm
  if (bot.health !== 20) {
    log("Bot took damage");
    fail();
  }
}

// Handles disconnection from the server by immediately failing the test
function onDisconnect() {
  log("Disconnected prematurely");
  fail();
}

// Handles a generic timeout of an event or otherwise by immediately failing the test
function onTimeout() {
  log("Timed out, failing")
  fail();
}

// Handles an unexpected error by immediately failing the test
function onError(err) {
  log("Unhandled error:", err);
  fail();
}

// Fails the test by terminating the process with an exit code
function fail(): never {
  log("###############################");
  log("# BOT INTEGRATION TEST FAILED #");
  log("###############################");
  process.exit(1);
}

// Successfully exits the test by terminating the process with an exit code. Mineflayer seems
// reluctant to exit on its own, even upon disconnection from the server, so its easiest to
// exit forcibly even in the event of a success
function succeed(): never {
  log("##################################");
  log("# BOT INTEGRATION TEST SUCCEEDED #");
  log("##################################");
  process.exit(0);
}

// Print messages to console prefixed by the time and a label expressing that the bot is
// responsible for the output
function log(...messages: any[]) {
  const currentDate = new Date();

  const hour = currentDate.getHours().toString().padStart(2, "0");
  const minute = currentDate.getMinutes().toString().padStart(2, "0");
  const second = currentDate.getSeconds().toString().padStart(2, "0");
  const millisecond = currentDate.getMilliseconds().toString().padStart(3, "0");

  console.log(`[BOT ${hour}:${minute}:${second}.${millisecond}]`, ...messages);
}
