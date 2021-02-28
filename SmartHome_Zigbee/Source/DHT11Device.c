#include "smartHome.h"
#include "sapi.h"
#include "hal_led.h"
#include"onBoard.h"
#include "hal_uart.h"
#include "stdio.h"

#define uchar unsigned char 
#define uint unsigned int
#define  DHT11_DATA  P0_4
#if 1
  #define NUM_LED_1 1
  #define NUM_LED_2 2
#elif 1
  #define NUM_LED_1 3
  #define NUM_LED_2 4
#else
  #define NUM_LED_1 5
  #define NUM_LED_2 6
#endif

#define LEDNUM  2
uint8 ledIdList[LEDNUM]={NUM_LED_1,NUM_LED_2};

#define NUM_IN_CMD_LEDDEVICE 1
#define NUM_OUT_CMD_LEDDEVICE 2

/*****************全局变量的定义******************/
uchar  Overtime_counter;  //判断等待是否超时的计数器。利用uchar型的数值范围进行自动延时控制（时长由初值决定），并判断是否超时
uchar  bit_value;          //从DATA总线上读到的位值
uchar  T_data_H, T_data_L, RH_data_H, RH_data_L, checkdata;//校验过的温度高8位,温度低8位,湿度高8位,湿度低8位,校验和8位
uchar  T_data_H_temp, T_data_L_temp, RH_data_H_temp, RH_data_L_temp, checkdata_temp;//未经校验的数据
uchar  comdata;            //从DHT11读取的一个字节的数据

char  str[16];


const cId_t ledDeviceInputCommandList[NUM_IN_CMD_LEDDEVICE]=
                                {TOGGLE_LED_CMD_ID};
const cId_t ledDeviceOutputCommandList[NUM_OUT_CMD_LEDDEVICE]=
                                {LEDJOINNET_CMD_ID,HEART_BEAT_CMD_ID};
const SimpleDescriptionFormat_t zb_SimpleDesc=
{
  ENDPOINT_ID_SMARTHOME,
  PROFILE_ID_SMARTHOME,
  DEVICE_ID_LEDDEVICE,
  DEVIDE_VERSION_ID,
  0,
  NUM_IN_CMD_LEDDEVICE,
  (cId_t*)ledDeviceInputCommandList,
  NUM_OUT_CMD_LEDDEVICE,
  (cId_t*)ledDeviceOutputCommandList  
};

void Read_Byte(void);

/*****************从DHT11读取一个字节函数******************/        
void  Read_Byte(void)
{
  uchar i;
  for (i = 0; i < 8; i++)                     //循环8次，读取8bit的数据
  {
    Overtime_counter = 2;                          //读取并等待DHT11发出的12-14us低电平开始信号
    P0DIR &= ~0x10;
    while ((!DHT11_DATA) && Overtime_counter++);
    //Delay_10us(80);                   //26-28us的低电平判断门限
    MicroWait(27);
    bit_value = 0;                          //跳过门限后判断总线是高还是低，高为1，低为0
    if(DHT11_DATA)
    bit_value = 1;
    Overtime_counter=2;                          //等待1bit的电平信号结束，不管是0是1在118us后都变为低电平，否则错误超时
    while (DHT11_DATA && Overtime_counter++);  //当U8FLAG加到255后溢出为0，跳出循环，并后加加为1
    if (Overtime_counter == 1)
      break;                           //超时则跳出for循环        
    comdata <<= 1;                      //左移1位，LSB补0
    comdata |= bit_value;                  //LSB赋值
  }
}


int Read_DHT11(unsigned char *temp,unsigned char *humid);
/*****************DHT11读取五个字节函数******************/
/*
功能描述：获取dht11的温湿度
参数说明：
    *temp：返回温度的整数部分
    *humid：返回湿度的整数部分
返回值：
  0：成功读取
  <0：读取失败
*/
int Read_DHT11(unsigned char *temp,unsigned char *humid)
{
    int result=-1;
    uchar checksum;
    P0DIR |= 0x10;
    DHT11_DATA = 0;                //主机拉低18ms
    MicroWait(18000);
    DHT11_DATA = 1;                //总线由上拉电阻拉高 主机延时20us-40us    
    MicroWait(35);
    DHT11_DATA = 1;                //主机转为输入或者输出高电平，DATA线由上拉电阻拉高，准备判断DHT11的响应信号
    P0DIR &= ~0x10;
    if (!DHT11_DATA)                //判断从机是否有低电平响应信号 如不响应则跳出，响应则向下运行        
    {
      Overtime_counter = 2;   //判断DHT11发出的80us的低电平响应信号是否结束
      while ((!DHT11_DATA)&&Overtime_counter++);
      Overtime_counter=2;   //判断DHT11是否发出80us的高电平，如发出则进入数据接收状态
      while ((DHT11_DATA)&&Overtime_counter++);
      Read_Byte();                //读取湿度值整数部分的高8bit
      RH_data_H_temp = comdata;
      Read_Byte();                //读取湿度值小数部分的低8bit
      RH_data_L_temp = comdata;
      Read_Byte();                //读取温度值整数部分的高8bit
      T_data_H_temp = comdata;
      Read_Byte();                //读取温度值小数部分的低8bit
      T_data_L_temp = comdata;
      Read_Byte();                //读取校验和的8bit
      checkdata_temp = comdata;
      P0DIR |= 0x10;
      DHT11_DATA = 1;                //读完数据将总线拉高
      checksum = (T_data_H_temp + T_data_L_temp + RH_data_H_temp + RH_data_L_temp);//进行数据校验
      if (checksum == checkdata_temp)
        {
          RH_data_H = RH_data_H_temp;//保存湿度的整数部分
          RH_data_L = RH_data_L_temp;//保存湿度的小数部分
          T_data_H  = T_data_H_temp;//保存温度的整数部分
          T_data_L  = T_data_L_temp;//保存温度的小数部分
          checkdata = checkdata_temp;
          *temp = T_data_H;
          *humid = RH_data_H;
          result=0;
        }
      
    }
    return result;
}

/***********
执行时机：发送的数据包被接收方收到时被调用
handle:包的编号；
status:ZSUCCESS表示成功接收
************/
void zb_SendDataConfirm( uint8 handle, uint8 status )
{
  
}

/***********
执行时机：接收到的数据包被调用
************/
void zb_ReceiveDataIndication( uint16 source, uint16 command, 
                              uint16 len, uint8 *pData  )
{
  
}


void zb_AllowBindConfirm( uint16 source )
{
}

void zb_HandleKeys( uint8 shift, uint8 keys )
{
  
}

void zb_BindConfirm( uint16 commandId, uint8 status )
{
}


//void zb_SendDataRequest ( uint16 destination, uint16 commandId, uint8 len,
//                          uint8 *pData, uint8 handle, uint8 ack, uint8 radius );
void zb_StartConfirm( uint8 status )
{
  if(status==ZSUCCESS)
  {   
    halUARTCfg_t uartcfg;
    uartcfg.baudRate=HAL_UART_BR_115200;
    uartcfg.flowControl=FALSE;
    uartcfg.callBackFunc=NULL;
    HalUARTOpen(0,&uartcfg);
    HalUARTWrite(0,"join success\r\n",osal_strlen("join success\r\n"));
    //可把节点所包含的led灯的ID号发送过去
    zb_SendDataRequest(0X0,LEDJOINNET_CMD_ID,LEDNUM,ledIdList,0,FALSE,AF_DEFAULT_RADIUS);
    osal_start_timerEx(sapi_TaskID,TIMER_TIMEOUT_EVT,2000);   
    osal_start_timerEx(sapi_TaskID,READ_DHT11_EVENT,5000);
  }
}

void zb_HandleOsalEvent( uint16 event )
{
  if(event&READ_DHT11_EVENT){//读取DHT11数据事件
    unsigned  char temphumi[2];
    Read_DHT11(temphumi,temphumi+1);
    if(temphumi!=NULL)
    {
      osal_start_timerEx(sapi_TaskID,READ_DHT11_EVENT,5000);
      //char buffer[50];
      //sprintf(buffer,"temp=%d,humid=%d\r\n",temp,humid);
      //sprintf(buffer,"{\"temp\":\"%d\"}",temp);
      //HalUARTWrite(0,(uint8*)buffer,osal_strlen(buffer)); 
      zb_SendDataRequest(0X0,TEMP_HUMI_CMD_ID,osal_strlen(temphumi),temphumi,0,FALSE,AF_DEFAULT_RADIUS);
      //sprintf(buffer,"{\"humi\":\"%d\"}",humid);
      HalUARTWrite(0,temphumi,osal_strlen(temphumi));
      //zb_SendDataRequest(0X0,TEMP_HUMI_CMD_ID,osal_strlen(buffer),buffer,0,FALSE,AF_DEFAULT_RADIUS);
      
     }
   }
  
  if(event&TIMER_TIMEOUT_EVT){
      osal_start_timerEx(sapi_TaskID,TIMER_TIMEOUT_EVT,2000);
      zb_SendDataRequest(0X0,HEART_BEAT_CMD_ID,0,NULL,0,FALSE,AF_DEFAULT_RADIUS); //发送心跳包 
   }
}

void zb_FindDeviceConfirm( uint8 searchType, 
                          uint8 *searchKey, uint8 *result )
{
  
}