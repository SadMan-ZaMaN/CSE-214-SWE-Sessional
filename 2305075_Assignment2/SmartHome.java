import java.util.*;

interface SmartDevice{
    void activate();
    void deactivate();
    double getPowerUsage();
    String getStatus();

    default Class<?> getDeviceType(){
        return this.getClass();
    }
}

abstract class AbstractSmartDevice implements SmartDevice{
    protected boolean on = false;

    @Override
    public void activate(){
        on = true;
    }
    @Override
    public void deactivate(){
        on = false;
    }
}

class SmartLight extends AbstractSmartDevice{
    @Override
    public double getPowerUsage(){
        double p = on ? 10.0 : 0.0;
        return p;
    }
    @Override
    public String getStatus(){
        String s = "Light: " + (on ? "ON" : "OFF");
        return s;
    }
}

class SmartThermostat extends AbstractSmartDevice{
    @Override
    public double getPowerUsage(){
        double p = on ? 150.0 : 0.0;
        return p;
    }
    @Override
    public String getStatus(){
        String s = "Thermostat: " + (on ? "ON" : "OFF");
        return s;
    }
}

class SmartSpeaker extends AbstractSmartDevice{
    @Override
    public double getPowerUsage(){
        double p = on ? 5.0 : 0.0; 
        return p;
    }
    @Override
    public String getStatus(){
        String s = "Speaker: " + (on ? "Playing" : "Idle");
        return s;
    }
}

interface CompositeDevice extends SmartDevice {
    List<SmartDevice> getChildren();
}


class Room implements CompositeDevice{
    private final String name;
    private final List<SmartDevice> devices = new ArrayList<>();

    Room(String name) {
        this.name = name;
    }

    public void addDevice(SmartDevice d) {
        devices.add(d);
    }

    @Override
    public List<SmartDevice> getChildren() {
        return devices;
    }

    @Override
    public void activate() {
        for (int i = 0; i < devices.size(); i++) {
            SmartDevice d = devices.get(i);
            d.activate();
        }
    }

    @Override
    public void deactivate() {
        for (int i = 0; i < devices.size(); i++) {
            SmartDevice d = devices.get(i);
            d.deactivate();
        }
    }

    @Override
    public double getPowerUsage() {
        double totalPower = 0.0;
        for (int i = 0; i < devices.size(); i++) {
            SmartDevice d = devices.get(i);
            totalPower = totalPower + d.getPowerUsage();
        }
        return totalPower;
    }

    @Override
    public String getStatus() {
        String statusText = "[" + name + "]";
        for (int i = 0; i < devices.size(); i++) {
            SmartDevice d = devices.get(i);
            statusText = statusText + "\n  " + d.getStatus();
        }
        return statusText;
    }
}



class Home implements CompositeDevice{
    private final String name;
    private final List<SmartDevice> rooms = new ArrayList<>();

    public Home(String name) {
        this.name = name;
    }

    public void addRoom(SmartDevice room) {
        rooms.add(room);
    }

    @Override
    public List<SmartDevice> getChildren() {
        return rooms;
    }

    @Override
    public void activate() {
        for (int i = 0; i < rooms.size(); i++) {
            SmartDevice r = rooms.get(i);
            r.activate();
        }
    }

    @Override
    public void deactivate() {
        for (int i = 0; i < rooms.size(); i++) {
            SmartDevice r = rooms.get(i);
            r.deactivate();
        }
    }

    @Override
    public double getPowerUsage() {
        double totalPower = 0.0;
        for (int i = 0; i < rooms.size(); i++) {
            SmartDevice r = rooms.get(i);
            totalPower = totalPower + r.getPowerUsage();
        }
        return totalPower;
    }

    @Override
    public String getStatus() {
        String statusText = "[Home: " + name + "]";
        for (int i = 0; i < rooms.size(); i++) {
            SmartDevice r = rooms.get(i);
            statusText = statusText + "\n" + r.getStatus();
        }
        return statusText;
    }
}



abstract class SmartDeviceDecorator implements SmartDevice {
    protected SmartDevice wrapped;

    public SmartDeviceDecorator(SmartDevice wrapped){
        this.wrapped = wrapped;
    }

    @Override
    public void activate(){
        wrapped.activate();
    }

    @Override
    public void deactivate(){
        wrapped.deactivate();
    }

    @Override
    public double getPowerUsage(){
        return wrapped.getPowerUsage();
    }

    @Override
    public String getStatus(){
        return wrapped.getStatus();
    }

    @Override
    public Class<?> getDeviceType(){
        return wrapped.getDeviceType(); 
    }
}



class AccessRestricted extends SmartDeviceDecorator{
    private final int pin;
    private boolean locked = true; 

    public AccessRestricted(SmartDevice wrapped, int pin){
        super(wrapped);
        this.pin = pin;
    }

    public void unlock(int enteredPin){
        if (enteredPin == pin) {
            locked = false;
        }
    }

    public void lock(){
        locked = true;
    }

    @Override
    public void activate(){
        if (!locked){
            wrapped.activate();
        }
    }

    @Override
    public void deactivate(){
        if (!locked){
            wrapped.deactivate();
        }
    }

    @Override
    public double getPowerUsage(){ 
        return wrapped.getPowerUsage(); 
    }

    @Override
    public String getStatus(){
        return wrapped.getStatus() + (locked ? " [LOCKED]" : "");
    }
}


class TimerControlled extends SmartDeviceDecorator {
    private final int seconds;
    private boolean running = false;

    public TimerControlled(SmartDevice wrapped, int seconds){
        super(wrapped);
        this.seconds = seconds;
    }

    @Override
    public void activate(){
        wrapped.activate();
        running = true;
    }

    @Override
    public void deactivate(){
        wrapped.deactivate();
        running = false; 
    }

    public void simulateTimerExpiry(){
        if (running){
            wrapped.deactivate();
            running = false;
        }
    }

    @Override
    public double getPowerUsage(){
        return wrapped.getPowerUsage();
    }

    @Override
    public String getStatus(){
        String s = wrapped.getStatus();
        if(running){
            s = s + " (auto-off in " + seconds + "s)";
        }
        return s;
    }
}

class PowerThrottled extends SmartDeviceDecorator{
    private final double cap;

    public PowerThrottled(SmartDevice wrapped, double cap){
        super(wrapped);
        this.cap = cap;
    }

    @Override
    public void activate(){
        wrapped.activate();
    }

    @Override
    public void deactivate(){
        wrapped.deactivate();
    }

    @Override
    public double getPowerUsage(){
        double raw = wrapped.getPowerUsage();
        if (raw > cap) {
            return cap;
        } else {
            return raw;
        }
    }

    @Override
    public String getStatus(){
        double raw = wrapped.getPowerUsage();
        String s = wrapped.getStatus();
        if (raw > cap){
            s = s + " [throttled to " + cap + "W]";
        }
        return s;
    }
}



class EcoMode implements CompositeDevice{
    private final CompositeDevice wrapped;
    private final double budget;

    public EcoMode(CompositeDevice wrapped, double budget){
        this.wrapped = wrapped;
        this.budget = budget;
    }

    @Override
    public List<SmartDevice> getChildren(){
        return wrapped.getChildren();
    }

    @Override
    public Class<?> getDeviceType() {
        return wrapped.getDeviceType();
    }

    @Override
    public void activate() {
        wrapped.activate();
        enforceBudget();
    }

    private void enforceBudget(){
        List<SmartDevice> children = wrapped.getChildren();

        for (int i = children.size() - 1;i>=0;i--){
            if (wrapped.getPowerUsage()<=budget){
                return;
            }
            children.get(i).deactivate();
        }
    }

    @Override
    public void deactivate() {
        wrapped.deactivate();
    }

    @Override
    public double getPowerUsage() {
        return wrapped.getPowerUsage();
    }

    @Override
    public String getStatus() {
        return "[ECO: " + budget + "W budget]\n" + wrapped.getStatus();
    }
}



class GuestMode implements CompositeDevice{
    private final CompositeDevice innerDevice;
    private final Set<Class<?>> allowedTypes;

    public GuestMode(CompositeDevice innerDevice, Set<Class<?>> allowedTypes){
        this.innerDevice = innerDevice;
        this.allowedTypes = allowedTypes;
    }

    @Override
    public List<SmartDevice> getChildren(){
        return innerDevice.getChildren();
    }

    @Override
    public Class<?> getDeviceType(){
        return innerDevice.getDeviceType();
    }

    @Override
    public void activate(){
        List<SmartDevice> devices = innerDevice.getChildren();
        
        for(int i = 0; i < devices.size(); i++){
            SmartDevice d = devices.get(i);
            if(isAllowed(d)){
                d.activate();
            }
        }
    }

    @Override
    public void deactivate(){
        List<SmartDevice> devices = innerDevice.getChildren();
        
        for(int i = 0; i < devices.size(); i++){
            SmartDevice d = devices.get(i);
            if(isAllowed(d)){
                d.deactivate();
            }
        }
    }

    @Override
    public double getPowerUsage(){
        double totalPower = 0.0;
        List<SmartDevice> devices = innerDevice.getChildren();
        
        for(int i = 0; i < devices.size(); i++){
            SmartDevice d = devices.get(i);
            if(isAllowed(d)){
                totalPower = totalPower + d.getPowerUsage();
            }
        }
        
        return totalPower;
    }

    @Override
    public String getStatus(){
        String status = "[GUEST MODE]";
        List<SmartDevice> devices = innerDevice.getChildren();
        
        for(int i = 0; i < devices.size(); i++){
            SmartDevice d = devices.get(i);
            status = status + "\n  " + d.getStatus();
            
            if(!isAllowed(d)){
                status = status + " [guest-restricted]";
            }
        }
        
        return status;
    }

    private boolean isAllowed(SmartDevice device){
        return allowedTypes.contains(device.getDeviceType());
    }
}










public class SmartHome {

    public static void main(String[] args) {
        demoA_HomeOverview();
        demoB_StackedUpgrades();
        demoC_EcoMode();
        demoD_OrderMatters();
        demoE_GuestMode();
        demoF_UpgradeAnEntireRoom();
    }

    static void header(String title) {
        System.out.println("\n" + "=".repeat(55));
        System.out.println("  " + title);
        System.out.println("=".repeat(55));
    }

    static void demoA_HomeOverview() {
        header("DEMO A: Home Overview");

        Room living = new Room("Living Room");
        living.addDevice(new SmartLight());
        living.addDevice(new SmartSpeaker());

        Room bedroom = new Room("Bedroom");
        bedroom.addDevice(new SmartLight());
        bedroom.addDevice(new SmartThermostat());

        Home home = new Home("My Home");
        home.addRoom(living);
        home.addRoom(bedroom);

        System.out.println("Before activation:");
        System.out.println(home.getStatus());
        System.out.println("Power: " + home.getPowerUsage() + "W");

        home.activate();
        System.out.println("\nAfter activation:");
        System.out.println(home.getStatus());
        System.out.println("Power: " + home.getPowerUsage() + "W");
    }

    static void demoB_StackedUpgrades() {
        header("DEMO B: AccessRestricted + TimerControlled");

        SmartLight bulb = new SmartLight();
        AccessRestricted ar = new AccessRestricted(bulb, 1234);
        TimerControlled tc = new TimerControlled(ar, 60);

        System.out.println("Step 1 — Activate while locked:");
        tc.activate();
        System.out.println("  Status: " + tc.getStatus());
        System.out.println("  Power:  " + tc.getPowerUsage() + "W");

        System.out.println("\nStep 2 — Wrong PIN:");
        ar.unlock(0000);
        System.out.println("  Status: " + tc.getStatus());

        System.out.println("\nStep 3 — Correct PIN, activate:");
        ar.unlock(1234);
        tc.activate();
        System.out.println("  Status: " + tc.getStatus());
        System.out.println("  Power:  " + tc.getPowerUsage() + "W");

        System.out.println("\nStep 4 — Timer expires:");
        tc.simulateTimerExpiry();
        System.out.println("  Status: " + tc.getStatus());
        System.out.println("  Power:  " + tc.getPowerUsage() + "W");
    }

    static void demoC_EcoMode() {
        header("DEMO C: EcoMode (budget = 100W)");

        Room office = new Room("Office");
        office.addDevice(new SmartLight());
        office.addDevice(new SmartLight());
        office.addDevice(new SmartThermostat());

        SmartDevice eco = new EcoMode(office, 100);

        System.out.println("Activating with EcoMode:");
        eco.activate();
        System.out.println("\n" + eco.getStatus());
        System.out.println("Power: " + eco.getPowerUsage() + "W");
    }

    static void demoD_OrderMatters() {
        header("DEMO D: Order Matters");

        // Setup 1: Throttled thermostat
        Room lab1 = new Room("Lab-1");
        lab1.addDevice(new SmartLight());
        lab1.addDevice(new SmartLight());
        lab1.addDevice(new PowerThrottled(new SmartThermostat(), 80));
        SmartDevice eco1 = new EcoMode(lab1, 100);

        System.out.println("Setup 1: Throttled thermostat (80W) + EcoMode(100W)");
        eco1.activate();
        System.out.println(eco1.getStatus());
        System.out.println("Power: " + eco1.getPowerUsage() + "W");

        // Setup 2: Raw thermostat
        Room lab2 = new Room("Lab-2");
        lab2.addDevice(new SmartLight());
        lab2.addDevice(new SmartLight());
        lab2.addDevice(new SmartThermostat());
        SmartDevice eco2 = new EcoMode(lab2, 100);

        System.out.println("\nSetup 2: Raw thermostat (150W) + EcoMode(100W)");
        eco2.activate();
        System.out.println(eco2.getStatus());
        System.out.println("Power: " + eco2.getPowerUsage() + "W");

        System.out.println("\n>> Same budget, different outcome depending on decoration order:");
        System.out.println("   Setup 1 (throttle-then-eco) keeps all 3 devices: "
                + eco1.getPowerUsage() + "W");
        System.out.println("   Setup 2 (raw-then-eco) sheds the thermostat entirely: "
                + eco2.getPowerUsage() + "W");
    }

    static void demoE_GuestMode() {
        header("DEMO E: GuestMode + Mixed Enhancements");

        Room guestRoom = new Room("Guest Room");
        guestRoom.addDevice(new SmartSpeaker());

        SmartThermostat thermo = new SmartThermostat();
        guestRoom.addDevice(new AccessRestricted(thermo, 9999));

        SmartLight timedLight = new SmartLight();
        guestRoom.addDevice(new TimerControlled(timedLight, 120));

        Set<Class<?>> allowed = new HashSet<>(Arrays.asList(SmartLight.class, SmartSpeaker.class));
        SmartDevice gm = new GuestMode(guestRoom, allowed);

        System.out.println("Activating GuestMode room:");
        gm.activate();
        System.out.println("\n" + gm.getStatus());
        System.out.println("Guest-visible power: " + gm.getPowerUsage() + "W");
    }

    static void demoF_UpgradeAnEntireRoom() {
        header("DEMO F: prepareForNight wraps a whole Room");

        Room kids = new Room("Kids Room");
        kids.addDevice(new SmartLight());
        kids.addDevice(new SmartSpeaker());
        kids.addDevice(new SmartThermostat());

        
        AccessRestricted lockedRoom = new AccessRestricted(kids, 0);
        TimerControlled night = new TimerControlled(lockedRoom, 3600);

        System.out.println("Step 1 — Activate while locked (nothing happens):");
        night.activate();
        System.out.println("  Status:\n" + night.getStatus());
        System.out.println("  Power: " + night.getPowerUsage() + "W");

        System.out.println("\nStep 2 — Unlock and activate:");
        lockedRoom.unlock(0);
        night.activate();
        System.out.println("  Status:\n" + night.getStatus());
        System.out.println("  Power: " + night.getPowerUsage() + "W");

        System.out.println("\nStep 3 — Timer expires (entire room shuts off):");
        night.simulateTimerExpiry();
        System.out.println("  Status:\n" + night.getStatus());
        System.out.println("  Power: " + night.getPowerUsage() + "W");

        System.out.println("\nStep 4 — Add to Home (works with no special-casing):");
        Home home = new Home("Night Home");
        home.addRoom(night);
        System.out.println("  Home power: " + home.getPowerUsage() + "W");
    }
}