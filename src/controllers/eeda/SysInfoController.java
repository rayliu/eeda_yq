package controllers.eeda;import java.io.BufferedReader;import java.io.FileInputStream;import java.io.IOException;import java.io.InputStreamReader;import java.math.BigDecimal;import java.util.ArrayList;import java.util.HashMap;import java.util.List;import java.util.Map;import java.util.StringTokenizer;import com.jfinal.core.Controller;import com.jfinal.log.Log;import com.jfinal.plugin.activerecord.Record;public class SysInfoController extends Controller {    private static Log logger = Log.getLog(SysInfoController.class);    private static final int CPUTIME = 500;    private static final int PERCENT = 100;    private static final int FAULTLENGTH = 10;    public void index() {        render("/eeda/index/monitor.html");    }        /**     * 功能：获取Linux系统cpu使用率     * */    public void cpuUsage() {        float cpusage = 0;         try {            Map<?, ?> map1 = cpuinfo();            Thread.sleep(5 * 1000);            Map<?, ?> map2 = cpuinfo();            long user1 = Long.parseLong(map1.get("user").toString());            long nice1 = Long.parseLong(map1.get("nice").toString());            long system1 = Long.parseLong(map1.get("system").toString());            long idle1 = Long.parseLong(map1.get("idle").toString());            long user2 = Long.parseLong(map2.get("user").toString());            long nice2 = Long.parseLong(map2.get("nice").toString());            long system2 = Long.parseLong(map2.get("system").toString());            long idle2 = Long.parseLong(map2.get("idle").toString());            long total1 = user1 + system1 + nice1;            long total2 = user2 + system2 + nice2;            float total = total2 - total1;            long totalIdle1 = user1 + nice1 + system1 + idle1;            long totalIdle2 = user2 + nice2 + system2 + idle2;            float totalidle = totalIdle2 - totalIdle1;            cpusage = (total / totalidle) * 100;        } catch (InterruptedException e) {            e.printStackTrace();        }        BigDecimal b = new BigDecimal(cpusage);        double mem = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();        renderText(String.valueOf(mem));    }    /**     * 功能：CPU使用信息     * */    public static Map<?, ?> cpuinfo() {        InputStreamReader inputs = null;        BufferedReader buffer = null;        Map<String, Object> map = new HashMap<String, Object>();        try {            inputs = new InputStreamReader(new FileInputStream("/proc/stat"));            buffer = new BufferedReader(inputs);            String line = "";            while (true) {                line = buffer.readLine();                if (line == null) {                    break;                }                if (line.startsWith("cpu")) {                    StringTokenizer tokenizer = new StringTokenizer(line);                    List<String> temp = new ArrayList<String>();                    while (tokenizer.hasMoreElements()) {                        String value = tokenizer.nextToken();                        temp.add(value);                    }                    map.put("user", temp.get(1));                    map.put("nice", temp.get(2));                    map.put("system", temp.get(3));                    map.put("idle", temp.get(4));                    map.put("iowait", temp.get(5));                    map.put("irq", temp.get(6));                    map.put("softirq", temp.get(7));                    map.put("stealstolen", temp.get(8));                    break;                }            }        } catch (Exception e) {            e.printStackTrace();        } finally {            try {                buffer.close();                inputs.close();            } catch (Exception e2) {                e2.printStackTrace();            }        }        return map;    }          // 获取内存使用率    public void getMem() throws Exception {        double usage =  0;        Map<String, Object> map = new HashMap<String, Object>();        InputStreamReader inputs = null;        BufferedReader buffer = null;        try {            inputs = new InputStreamReader(new FileInputStream("/proc/meminfo"));            buffer = new BufferedReader(inputs);            String line = "";            while (true) {                line = buffer.readLine();                if (line == null)                    break;                int beginIndex = 0;                int endIndex = line.indexOf(":");                if (endIndex != -1) {                    String key = line.substring(beginIndex, endIndex);                    beginIndex = endIndex + 1;                    endIndex = line.length();                    String memory = line.substring(beginIndex, endIndex);                    String value = memory.replace("kB", "").trim();                    map.put(key, value);                }            }            long memTotal = Long.parseLong(map.get("MemTotal").toString());            long memFree = Long.parseLong(map.get("MemFree").toString());            long memused = memTotal - memFree;            long buffers = Long.parseLong(map.get("Buffers").toString());            long cached = Long.parseLong(map.get("Cached").toString());            usage = (double) (memused - buffers - cached) / memTotal * 100;        } catch (Exception e) {            e.printStackTrace();        } finally {            try {                buffer.close();                inputs.close();            } catch (Exception e2) {                e2.printStackTrace();            }        }                BigDecimal b = new BigDecimal(usage);        double mem = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();        renderText(String.valueOf(mem));        // return compare.toString();    }    // 获取文件系统使用率    public void getDisk() throws IOException {        double totalhd = 0;        double usedhd = 0;        Runtime rt = Runtime.getRuntime();        Process p = rt.exec("df -hl /home");// df -hl 查看硬盘空间        BufferedReader in = null;        try {            in = new BufferedReader(new InputStreamReader(p.getInputStream()));            String str = null;            String[] strArray = null;            while ((str = in.readLine()) != null) {                int m = 0;                strArray = str.split(" ");                for (String tmp : strArray) {                    if (tmp.trim().length() == 0)                        continue;                    ++m;                    System.out.println("----tmp----" + tmp);                    if (tmp.indexOf("G") != -1) {                        if (m == 2) {                            System.out.println("---G----" + tmp);                            if (!tmp.equals("") && !tmp.equals("0"))                                usedhd += Double.parseDouble(tmp.substring(0,                                        tmp.length() - 1)) * 1024;                        }                        if (m == 3) {                            System.out.println("---G----" + tmp);                            if (!tmp.equals("none") && !tmp.equals("0"))                                totalhd+= Double.parseDouble(tmp.substring(0,                                        tmp.length() - 1)) * 1024;                        }                    }                    if (tmp.indexOf("M") != -1) {                        if (m == 2) {                            System.out.println("---M---" + tmp);                            if (!tmp.equals("") && !tmp.equals("0"))                                usedhd += Double.parseDouble(tmp.substring(0,                                        tmp.length() - 1));                        }                        if (m == 3) {                            System.out.println("---M---" + tmp);                            if (!tmp.equals("none") && !tmp.equals("0"))                                totalhd += Double.parseDouble(tmp.substring(0,                                        tmp.length() - 1));                            System.out.println("----3----" + totalhd);                        }                    }                }            }        } catch (Exception e) {            e.printStackTrace();        } finally {            in.close();        }        // 上面写在userd和total写反了，懒得改了，就反着用了        System.out.println("----totalhd----" + totalhd);        System.out.println("----usedhd----" + usedhd);        Record rec = new Record();        rec.set("un_used", totalhd-usedhd);        rec.set("used", usedhd);        renderJson(rec);//        return str;    }    /**     * 功能：可用磁盘     * */    public static int disk() {        try {            long total = 0;//FileSystemUtils.freeSpaceKb("/home");            double disk = (double) total / 1024 / 1024;            return (int) disk;        } catch (Exception e) {            e.printStackTrace();        }        return 0;    }}