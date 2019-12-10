package com.makepower.processmanager.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.DecimalFormat;
import java.text.ParseException;

import static java.util.stream.Collectors.joining;

/**
 * @Author czx
 * @Description cmd执行工具类
 * @Version 2019-10-09 12:49
 */
@Slf4j
@Component
public class CmdUtils {
    private static String cmdPath;
    private static String serverName;

    /**
     * 执行cmd命令,重启SQL server服务
     */
    public static void restartSqlserver() {
        try {
            Runtime.getRuntime().exec(cmdPath + "/nircmd.exe elevate net stop " + serverName);
            Thread.sleep(35000);
            Runtime.getRuntime().exec(cmdPath + "/nircmd.exe elevate net start " + serverName);
            log.info("SQL server重启成功");
        } catch (IOException | InterruptedException e) {
            log.error("执行cmd命令异常", e);
        }
    }

    /**
     * 杀死进程
     * @param pid 进程号
     */
    public static void killProcessByPid(Integer pid) throws IOException, InterruptedException {
        String command = "cmd /c taskkill /pid %d -f";
        Process exec = Runtime.getRuntime().exec( String.format(command, pid));
        int status = exec.waitFor();
        log.info("杀进程指令执行结果：{}", status);
//        InputStream in = exec.getInputStream();
//        BufferedReader br = new BufferedReader(new InputStreamReader(in));
//        String line = br.readLine();
//        while(line!=null) {
//            System.out.println(line);
//            line = br.readLine();
//        }
        exec.destroy();
    }

    /**
     * 根据exe文件的地址,重启exe程序
     * @param exePath exe文件的全路径
     */
    public synchronized static void restartProcessByExePath(String exePath) throws IOException, InterruptedException {
        //start 后面加两个双引号可以解决路径中有空格的问题
        String commandTemp = "cmd /c taskkill /F /IM %s & start \"\" %s";
        //从exe文件的全路径中获取exe文件的名字
        String exeName = getExeNameByPath(exePath);
        String newExeName = exeName.endsWith("\"") ? "\""+exeName: exeName;
        String newExePath = exePath.startsWith("\"") ? exePath : "\""+exePath;
        String command = String.format(commandTemp, newExeName, newExePath);
        Process exec = Runtime.getRuntime().exec(command);
        int status = exec.waitFor();
        exec.destroy();
        log.info("重启进程指令执行结果：{}", status);
    }

    /**
     * 查询进程所占内存 单位是KB
     * @param processName 进程的名字
     * @return 内存大小，单位KB，如果程序未启动，就返回-1
     */
    public static Long getProcessMemoryByName(String processName) throws IOException, InterruptedException, ParseException {
        String commandTemp = "cmd /c tasklist | findstr %s";
        String command = String.format(commandTemp, processName);
        Process exec = Runtime.getRuntime().exec(command);
        int status = exec.waitFor();
        log.info("查内存指令{}执行结果：{}", command, status);
        //获取当前系统默认字符集
        String systemDefaultEncode = System.getProperty("sun.jnu.encoding");
        BufferedReader br = new BufferedReader(new InputStreamReader(exec.getInputStream(), systemDefaultEncode));
        String line = br.lines().collect(joining());
        if (line == null || line.length() <= 0){
            log.info("程序{}未启动", processName);
            return -1L;
        }
        //TODO 如果有子进程，可能会出现多行，暂时先不考虑，先把功能做出来了以后在考虑
        String[] strings = line.split("[ ]+");
        DecimalFormat df = new DecimalFormat("0,000");
        Long result = (Long) df.parse(strings[4]);
        exec.destroy();
        return result;
    }

    /**
     * 根据程序全路径获取这个程序当前占用的内存 单位是KB
     * @param exePath
     * @return 内存大小，单位KB
     */
    public static Long getProcessMemoryByPath(String exePath) throws IOException, InterruptedException, ParseException {
        String exeName = getExeNameByPath(exePath);
        return getProcessMemoryByName(exeName);
    }

    /**
     * 从exe文件的全路径名中获取exe文件名
     * @param exePath
     * @return
     */
    public static String getExeNameByPath(String exePath){
        if (exePath == null || exePath.length() <=0){
            throw new IllegalArgumentException("文件全路径名为null或空字符串："+exePath);
        }
        return exePath.substring(exePath.lastIndexOf(File.separator)+1);
    }


    @Value("${nirexe-path}")
    public void setCmdPath(String cmdPath){
        CmdUtils.cmdPath = cmdPath;
    }
    @Value("${sqlserver-name}")
    public void setServerName(String serverName){
        CmdUtils.serverName = serverName;
    }

}