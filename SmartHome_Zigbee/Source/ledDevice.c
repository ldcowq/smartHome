#include "smartHome.h"
#include "sapi.h"
#include "hal_led.h"
#include"onBoard.h"
#include "hal_uart.h"
#include "hal_adc.h"
#include "stdio.h"

#if 0
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
void Init_ADC(void);
void Init_ADC()
{
  ADCH &= 0x00;
  ADCL &= 0x00;      //清空ADC数据寄存器
  
  P1SEL &= ~0x02;    //设置P0.1为普通IO口
  P1DIR &= ~0x02;    //P0.1定义为输入口
  
  //APCFG |= 0x02;      //P0.1配置为模拟I/O口
  ADCCON3 = 0xB1;     //参考电压：VDD5 引脚;512 抽取率(12 位 );通道1
}

uint8 GetMq2(void);
//读取MQ2的浓度
uint8 GetMq2(void){
  uint16 adc= 0;
  float vol=0.0; //adc采样电压  
  uint8 percent=0;//百分比的整数值

  //读MQ2浓度
  adc= HalAdcRead(HAL_ADC_CHANNEL_0, HAL_ADC_RESOLUTION_14);

  //最大采样值8191(因为最高位是符号位)
  //2的13次方=8191
  if(adc>=8191)
  {
    return 0;
  }

  //转化为百分比
  vol=(float)((float)adc)/8191.0;
     
  //取百分比两位数字
  percent=vol*100;

  return percent;
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
  Init_ADC();
  HalAdcInit();
  if(status==ZSUCCESS)
  {
    //可把节点所包含的led灯的ID号发送过去    
    halUARTCfg_t uartcfg;
    uartcfg.baudRate=HAL_UART_BR_115200;
    uartcfg.flowControl=FALSE;
    uartcfg.callBackFunc=NULL;
    HalUARTOpen(0,&uartcfg);
    
    char buf[]="MQ2Device is joined successfully!\r\n";
    HalUARTWrite(0,buf,osal_strlen(buf));
    zb_SendDataRequest(0X0,LEDJOINNET_CMD_ID,LEDNUM,ledIdList,0,FALSE,AF_DEFAULT_RADIUS);
    osal_start_timerEx(sapi_TaskID,READ_MQ2_EVENT,3000);
  }
}

void zb_HandleOsalEvent( uint16 event )
{
  if(event&READ_MQ2_EVENT)
  {
   int8 result= GetMq2();
   Init_ADC();
   char buffer[20];
   sprintf(buffer,"result=%d\r\n",result);
   HalUARTWrite(0,buffer,osal_strlen(buffer));
        
   zb_SendDataRequest(0X0,HEART_BEAT_CMD_ID,0,NULL,0,FALSE,AF_DEFAULT_RADIUS); //读取完mq2数据，接着发一个心跳包    
   osal_start_timerEx(sapi_TaskID,READ_MQ2_EVENT,5000);//每四秒读取一次   
  }
}

void zb_FindDeviceConfirm( uint8 searchType, 
                          uint8 *searchKey, uint8 *result )
{
  
}