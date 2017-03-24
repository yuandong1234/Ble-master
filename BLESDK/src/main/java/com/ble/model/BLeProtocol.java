package com.ble.model;

/**
 * 蓝牙数据数据协议
 * Created by yuandong on 2017/3/23 0023.
 */

public class BLeProtocol {

    private static final long serialVersionUID = 1L;
    /**
     * 帧头(2个字节)
     */
    private byte[] head = null;

    /**
     * 数据长度(1个字节)
     */
    private byte length;

    /**
     * 帧编号(1个字节)
     */
    private byte frameNo;

    /**
     * 命令域(1个字节)
     */
    private byte command;

    /**
     * 数据域(n个字节)
     */
    private byte[] datas = null;

    /**
     * 校验位(1个字节)
     */
    private byte check;


    // 通讯数据
    private byte[] protocolComm = null;

    public BLeProtocol(byte[] head, byte lenght, byte frameNo, byte command,
                       byte[] datas, byte check) {
        this.head = head;
        this.length = lenght;
        this.frameNo = frameNo;
        this.command = command;
        this.datas = datas;
        this.check = check;
    }

    public BLeProtocol(byte[] protocolComm) {

        if (protocolComm.length == 6) {//没有数据域
            head = new byte[2];
            head[0] = protocolComm[0];
            head[1] = protocolComm[1];
            this.length = protocolComm[2];
            this.frameNo = protocolComm[3];
            this.command = protocolComm[4];
            this.check = protocolComm[5];
        } else if (protocolComm.length > 6) {

            head = new byte[2];
            head[0] = protocolComm[0];
            head[1] = protocolComm[1];
            this.length = protocolComm[2];
            this.frameNo = protocolComm[3];
            this.command = protocolComm[4];
            datas = new byte[length - 2];
            System.arraycopy(protocolComm, 5, datas, 0, datas.length);

            this.check = protocolComm[protocolComm.length - 1];
        }

        this.protocolComm = protocolComm;
    }


    public byte[] getHead() {
        return head;
    }

    public void setHead(byte[] head) {
        this.head = head;
    }

    public byte getLenght() {
        return length;
    }

    public void setLenght(byte lenght) {
        this.length = lenght;
    }

    /**
     * 获取帧编号
     *
     * @return 帧编号
     */
    public byte getFrameNo() {
        return frameNo;
    }

    public void setFrameNo(byte frameNo) {
        this.frameNo = frameNo;
    }

    /**
     * 获取帧命令域
     *
     * @return 命令域编码
     */
    public byte getCommand() {
        return command;
    }

    public void setCommand(byte command) {
        this.command = command;
    }

    /**
     * 获取数据域值
     *
     * @return 数据域数组
     */
    public byte[] getDatas() {
        return datas;
    }

    public void setDatas(byte[] datas) {
        this.datas = datas;
    }

    public byte getCheck() {
        return check;
    }

    public void setCheck(byte check) {
        this.check = check;
    }

    // 数据协议有两种 1 数据域没数据 2 数据域有数据
    public byte[] getProtocolComm() {

        // 数据不为空直接返回
        if (protocolComm != null)
            return protocolComm;

        // 数据为空
        if (datas != null) {
            protocolComm = new byte[6];

            System.arraycopy(head, 0, protocolComm, 0, head.length);

            protocolComm[2] = length;

            protocolComm[3] = frameNo;

            protocolComm[4] = command;

            check = (byte) (protocolComm[2] ^ protocolComm[3] ^ protocolComm[4]);
            protocolComm[5] = check;
        } else {

            protocolComm = new byte[6 + datas.length];

            System.arraycopy(head, 0, protocolComm, 0, head.length);

            protocolComm[2] = length;

            protocolComm[3] = frameNo;

            protocolComm[4] = command;

            System.arraycopy(datas, 0, protocolComm, 5, datas.length);
            check = (byte) (protocolComm[2] ^ protocolComm[3] ^ protocolComm[4]);

            // 计算 校验码
            for (int i = 0; i < datas.length; i++) {
                check ^= datas[i];
            }
            protocolComm[5 + datas.length] = check;
        }

        return protocolComm;
    }
}
