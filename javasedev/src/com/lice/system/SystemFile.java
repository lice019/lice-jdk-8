package com.lice.system;

/**
 * description: SystemFile <br>
 * date: 2019/10/9 18:00 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class SystemFile {

    public String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "SystemFile{" +
                "name='" + name + '\'' +
                '}';
    }

    public static void main(String[] args) {
        SystemFile sf = new SystemFile();
        parse("lice", sf);
        System.out.println(sf);
    }

    public static SystemFile parse(String name, SystemFile sf) {

        sf.setName(name);
        return sf;
    }
}
