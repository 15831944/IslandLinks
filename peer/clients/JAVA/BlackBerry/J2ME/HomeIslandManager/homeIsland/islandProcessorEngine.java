/*
 * islandProcessorEngine.java
 *
 * � EKenDo,LLC, 2003-2009
 * Confidential and proprietary.
 */

package com.ekendotech.homeIsland.engine;


import java.util.*;
import java.lang.*;

import com.ekendotech.homeIsland.engine.processor.*;

/**
 * This class will perform tasks specific to island processor comands
 * whether the commands come from a local source, online source or
 * island source. The class knows how to run processor operations 
 * coming from a translation of a variety of architectures. This 
 * class will be directed by the Software Engine and more specifically,
 * managed by the KernelExecutionDetails class. 
 */
 
public class islandProcessorEngine //implements Runnable
{
    // class flags
    private boolean debug;
    private boolean machineInitialized;
    private boolean no_dbct;
    private boolean can_step_disassemble;
    
    private int tb_tbt_size;
    private int tb_tbp_size;
    private int offsetValue;
    
    private int procEngineState;
    private int procEngineStatus;
    private int procMachineState;
    private int procMachineStatus;
    private int procStackState;
    private int procStackStatus;
    private int softKernelState;
    private int softKernelStatus;
    
    
    // Class Custom Objects
    private MachineConfig config;
    // Options - allegedy for dbct
    private Hashtable codeCov;
    // Arch Config?
    // hex as string
    private String offset; 
     
    // Instatiated memory data
    private Hashtable procMap; // main processor elements and events
    
    // Static objects
    private static Hashtable peInstructions = new Hashtable();
    private static Hashtable peJumpLinks = new Hashtable();
    private static Hashtable peJumps = new Hashtable();
    private static Hashtable peStackInstruction = new Hashtable();
    
    private static Stack peCallStack = new Stack();
    
    // General config values
    private static final int MAX_BANK = 8;
    private static final int MAX_STR = 1024;
    
    // General Def values
    private static final int LOW = 0;
    private static final int HIGH = 1;
    private static final int LOWHIGH = 1;
    private static final int HIGHLOW = 2;

    // Data sizes
    public Character DCB; //byte
    public Short DCH; // halfword
    public Integer DCW; // word
    public Long DCD; // double word
    
    // Engine Status
    //public static final int WAIT_FOR_DETAILS = 9;
    //public static final int WAIT_FOR_DOWNLOAD = 10;
    public static final int WAIT_FOR_EXTERNAL_FUNCTION = 11;
    public static final int WAIT_FOR_KERNEL_ACTION = 12;
    public static final int WAIT_FOR_ENGINE_STOP = 13;
    //public static final int WAIT_FOR_ENGINE_INITIALIZATION = 14;
    //public static final int WAIT_FOR_ENGINE_OPERATION = 15;
    public static final int WAIT_FOR_KERNEL_AWK = 16;
    public static final int WAIT_FOR_PROCESSOR_POINTS = 17;
    public static final int WAIT_FOR_MACHINE_INITIALIZATION = 55;
    public static final int WAIT_FOR_MACHINE_ACTION = 40; 
    
    // Engine State
    public static final int PROC_ENGINE_STOPPED = 18;
    public static final int PROC_ENGINE_STARTED = 19;
    public static final int PROC_MACH_INITIALIZED = 54;
    //public static final int PROC_MACH_CYCLING = 53;
    
    // Processor Operations
    public static final int PROCESSOR_COND = 20;
    public static final int PROCESSOR_JUMP = 21;
    public static final int PROCESSOR_DATA = 22;
    public static final int PROCESSOR_DATA_EXTND = 26;
    public static final int PROCESSOR_DATA_MISC = 28;
    public static final int PROCESSOR_MATH = 35;
    public static final int PROCESSOR_MATH_MULT = 23;
    public static final int PROCESSOR_MATH_ARITH = 27;
    public static final int PROCESSOR_MATH_PARA_ADD = 24;
    public static final int PROCESSOR_MATH_PARA_SUB = 25;
    public static final int PROCESSOR_REGISTER_STATUS = 29;
    public static final int PROCESSOR_REGISTER_LOAD = 30;
    public static final int PROCESSOR_REGISTER_STORE = 31;
    public static final int PROCESSOR_REGISTER_LOAD_MULT = 32;
    public static final int PROCESSER_REGISTER_STORE_MULT = 33;
    public static final int PROCESSOR_REGISTER_SEMAPHORE = 34;
    public static final int PROCESSOR_REGISTER_COPROCESSOR = 37;
    public static final int PROCESSOR_EXCEPTION = 36;
    public static final int PROCESSOR_LOOP = 38;
    public static final int PROCESSOR_KERNEL_FUNCTION = 39;
    //public static final int PROCESSOR_API_FUNCTION = 40;
    public static final int PROCESSOR_LOCAL_FUNCTION = 41;
    public static final int PROCESSOR_OPERATIONS = 51;
    //public static final int PROCESSOR_API_FRAMEWORK = 53;
    
    // Processor Events
    public static final int PROCESSOR_STACK_EVENT = 42;
    public static final int PROCESSOR_LINK_EVENT = 43;
    public static final int PROCESSOR_PROGRAM_COUNTER_EVENT = 44;
    
    // Processor Architectures
    public static final int PROCESSOR_ARM_ARCH = 43;
    public static final int PROCESSOR_x86_ARCH = 44;
    public static final int PROCESSOR_SPARC_ARCH = 45;
    public static final int PROCESSOR_PPC_ARCH = 46;
    public static final int PROCESSOR_BFIN_ARCH = 47;
    public static final int PROCESSOR_MIPS_ARCH = 48;
    public static final int PROCESSOR_COLDFIRE_ARCH = 49;
    public static final int PROCESSOR_M86_ARCH = 50;
    public static final int PROCESSOR_ARCHS = 52;
    
    // Processor Address/Memory spaces
    private static final int SYSTEM = 0;
    private static final int DEVICE_NS = 1;
    private static final int DEVICE_S = 2;
    private static final int RAM_WT = 3;
    private static final int RAM_WBWA = 4;
    private static final int PERIPHERAL = 5;
    private static final int SRAM = 6;
    private static final int CODE = 7;
        
    // Memory Types
    private static final int MEMTYPE_IO = 0;
    private static final int MEMTYPE_RAM = 1;
    private static final int MEMTYPE_ROM = 2;
    private static final int MEMTYPE_FLASH = 3;
    
    // Processor Cache Types
    
    // Processor object types
    private static final int PROC_STATE = 0;
    
    abstract public class ProcArch
    {
        public ProcArch()
        {
            
        }
        
        public abstract void SetPC();
        
        public abstract Long GetPC(); 
        
        public abstract boolean DoCycle();
        
        public abstract boolean Emulate();
        
        public abstract boolean StepOnce();
        
        public abstract void Init();
        
        public abstract void Reset();
        
        public abstract Hashtable GetState(); 
    };
        
        
    public class MachineConfig
    {
        private int Io_cycle_divisor; 
        private int Dev_count;
        
        // Arch Object
        private Object Machine; 
        
        // AddressMaps
        private Hashtable SystemAddreses;
        private Hashtable DeviceNonShareableAddress;
        private Hashtable DeviceShareableAddress;
        private Hashtable WT_RAMAddresses;
        private Hashtable WBWA_RAMAddresses;
        private Hashtable PeripheralAddresses;
        private Hashtable SRAMAddresses;
        private Hashtable CodeAddresses;
        
        // Address
        private String startAddress;
        private String gotoAddress;
        private String endAddress;
        
        // Values
        private String archName;
        
        // Configs
        private Hashtable DeviceDesc;
        private Hashtable State;
        
        // flags  
        private boolean usesMmu;
        private boolean usesCoProcs;
        private boolean usesVFPD;
        private boolean usesJazelle;
        private boolean canStepDisassemble;
        
        // event breaks
        private boolean throwEventAtInterrupt;
        private boolean throwEventAtStackCall;
        private boolean throwEventAtValueDelta;
        private boolean throwEventAtJump;
        
        public MachineConfig()
        {
            this.Io_cycle_divisor = 0;
            this.Dev_count = 0;
            
            this.Machine = null;
            
            SystemAddreses = new Hashtable();
            DeviceNonShareableAddress = new Hashtable();
            DeviceShareableAddress = new Hashtable();
            WT_RAMAddresses = new Hashtable();
            WBWA_RAMAddresses= new Hashtable();
            PeripheralAddresses = new Hashtable();
            SRAMAddresses = new Hashtable();
            CodeAddresses = new Hashtable();
            
            startAddress = "";
            gotoAddress = "";
            endAddress = "";
            
            this.archName = "";
            
            DeviceDesc = new Hashtable();
            State = new Hashtable();
            
            usesMmu = false;
            usesCoProcs = false;
            this.usesVFPD = false;
            canStepDisassemble = false;
        
            throwEventAtInterrupt = false;
            throwEventAtStackCall = false;
            throwEventAtValueDelta = false;
            throwEventAtJump = false;
        }
        
        public MachineConfig(String sa)
        {
            startAddress = sa;
        }
        
        public void UsesMMU(boolean mmu)
        {
            usesMmu = mmu;
        }
        
        public void UsesDBCT(boolean dbct)
        {
            
        }
        
        public boolean InitArch(int arch)
        {
            boolean done = false;
            ARM_Arch armProc;
            
            try
            {
                switch(arch)
                {
                    case islandProcessorEngine.PROCESSOR_ARM_ARCH:
                        armProc = new ARM_Arch();
                        this.archName = "ARM"; 
                        this.Io_cycle_divisor = armProc.GetIoCycleDivisor();
                        this.init(armProc.GetState());
                        this.Machine = armProc;                    
                        
                        break;
                }
            }
            catch(Exception ex)
            {
                System.out.println("[ERROR] problems Initializing Processor Architecture:"+ex.toString());
            }
            return done;
        }
        
        /* Gettors */
        public String GetName()
        {
            return this.archName;
        }
        
        /* Settors */
        public void SetStartAddress(String addy)
        {
            this.startAddress = addy;
        }

        public void SetEndAddress(String addy)
        {
            this.endAddress = addy;
        }
        
        public void SetGoToAddress(String addy)
        {
            this.gotoAddress = addy;
        }
        
        public void SetInterrupt()
        {
            
        }
        
        /* Actions */
        public void DoCycle()
        {
            
        }
        
        public void Reset()
        {
            
            
        }
        
        public void CreateMemorySpace()
        {
            
        } 
        
        /* memory reads */
        public int MemReadByte()
        {
            return 0;
        }
        
        /* memory writes */
        public int MemWriteByte()
        {
            return 0;
        }
        
        /* private functions */
        private void init(Hashtable state)
        {
            
        }
    }
    
    /**
     * Class links addresses to Data, Functions and
     * static elements.
     */
    public class V_CallStackElements
    {
        public int id;
        public int layoutId;
        
        public String functionName;
        public String resultAddressNum;
        public String resultAddressName;
        public Object resultValue;
        
        public Vector arguments;
        
        public boolean isData;
        public boolean isArgument;
        public boolean isKernelFunction;
        public boolean isApiFunction;
        public boolean isLocalFunction;
        
        public V_CallStackElements()
        {
            id  = 0;
            layoutId=0;
            
            // initialize strings
            resultAddressNum = "";
            resultAddressName = "";
            functionName = "";
            
            resultValue = null;
            
            // initialize flags
            isData = false;
            isArgument = false;
            isKernelFunction = false;
            isApiFunction = false;
            isLocalFunction = false;
        }
        
        public boolean AddArgument()
        {
            return false;
        }
        
        public boolean AddFunction(String functionName)
        {
            return false;   
        }
        
        public boolean AddFunction(String functionName, int type)
        {
            return false;   
        }
        
        public boolean AddAddress(String addy)
        {
            return false;
        }
        
        /* gettors and Settors */
        public void SetFuctionResult(Object result)
        {
            this.resultValue = result;
        }
        
        public Object GetFunctionResult()
        {
            return this.resultValue;
        }
    }
    
    /* Ported Architecture for ARM */ 
    public class ARM_Arch
    {
        // Coprocessor interface definitions
        public static final int FIRST = 0;
        public static final int TRANSFER =1;
        public static final int BUSY = 2;
        public static final int DATA = 3;
        public static final int INTERRUPT = 4;
    
        public static final int DONE = 0;
        public static final int CANT = 1;
        public static final int INC = 3;
        
        public static final int IMMED_TABLE_MAX = 4069;
        public static final int BIT_LIST_MAX = 256;
        public static final int MULT_TABLE_MAX = 32;
        
        public static final int SVC32MODE = 0;
        public static final int ABORT32MODE = 1;
        public static final int UNDEF32MODE = 2;
        
        /* memory layout */
        public static final String ADDRSUPERSTACK = "0x800L";
        public static final String ADDRUSERSTACK = "0x80000L";
        public static final String ADDSOFTVECTOR = "0x840L";
        public static final String ADDRCMDLINE = "0xf00L";
        public static final String ADDRSOFTHANDLERS = "0xad0L";
        public static final String SOFTVECTORCODE = "0xb80L";
    
        public static final String CP13_R0_FIQ = "0x1";
        public static final String CP13_R0_IRQ = "0x2";
        public static final String CP13_R8_PMUS = "0x1";
    
        public static final String CP14_R0_ENABLE = "0x0001";
        public static final String CP14_R0_CLKRST = "0x0004";
        public static final String CP14_R0_CCD = "0x0008";
        public static final String CP14_R0_INTEN0 = "0x0010";
        public static final String CP14_R0_INTEN1 = "0x0020";
        public static final String CP14_R0_INTEN2 = "0x0040";
        public static final String CP14_R0_FLAG0 = "0x0100";
        public static final String CP14_R0_FLAG1 = "0x0200";
        public static final String CP14_R0_FLAG2 = "0x0400";
        public static final String CP14_R10_MOE_IB = "0x0004";
        public static final String CP14_R10_MOE_DB = "0x0008";
        public static final String CP14_R10_MOE_BT = "0x000c";
        public static final String CP15_R1_ENDIAN = "0x0080";
        public static final String CP15_R1_ALIGN = "0x0002";
        public static final String CP15_R5_X = "0x0002";
        public static final String CP15_R5_ST_ALIGN = "0x0002";
        public static final String CP15_R5_IMPRE = "0x0002";
        public static final String CP15_R5_MMU_EXCPT = "0x0002";
        public static final String CP15_DBCON_M = "0x0002";
        public static final String CP15_DBCON_E1 = "0x0002";
        public static final String CP15_DBCON_E0 = "0x0002";
    
        //public long ARMword;
        //public int ARMbyte;
        //public char ARMhword;
    
        private MMU armMmu;
        private DACT armOp; 
        private MEM armIo;
        
        private String instr;
        
        private Hashtable armState;
        private Hashtable armCpu;
        private Hashtable armMem;
        
        private Hashtable conditionCodes;
        private Hashtable opCodes;
        private Hashtable osBlock; 
        private Hashtable machSettings;
        
        // exceptions
        private Vector armExceptions;
        
        // registers
        private Vector armRegisters;
        
        // co-processors
        private Vector coProcessorRegisters;
        
        private Vector presetRegfile;
        private Vector immedTable;
        private Vector bitList;
        private Vector softVectorCode;
        
        // SP
        private int stackPointer;
        
        // LP
        private int linkPointer;
        
        // PC       
        private int programCounter;
        
        // FP
        private int framePointer;
        
        // CPSR
        private int currentProgramStatusRegister;
        
        private int verbosity;
        private int mem_size;
        private int cacheType;
        
        
        // config
        boolean bigEndian;
        boolean debug;
        
        // Constructor(s)
        public ARM_Arch()
        {
            // set bool values
            boolean big_endian = false; 
            boolean debug = false;
            
            // initialize state
            this.createNewState();
            
            this.conditionCodes = new Hashtable();
            
            conditionCodes.put("EQ",new Integer(0));
            conditionCodes.put("NE",new Integer(1));
            conditionCodes.put("CS",new Integer(2));
            conditionCodes.put("CC",new Integer(3));
            conditionCodes.put("MI",new Integer(4));
            conditionCodes.put("PL",new Integer(5));
            conditionCodes.put("VS",new Integer(6));
            conditionCodes.put("VC",new Integer(7));
            conditionCodes.put("HI",new Integer(8));
            conditionCodes.put("LS",new Integer(9));
            conditionCodes.put("GE",new Integer(10));
            conditionCodes.put("LT",new Integer(11));
            conditionCodes.put("GT",new Integer(12));
            conditionCodes.put("LE",new Integer(13));
            conditionCodes.put("AL",new Integer(14));
            conditionCodes.put("NV",new Integer(15));
            
            this.opCodes = new Hashtable();
            
            opCodes.put("LSL",new Integer(0));
            opCodes.put("LSR",new Integer(1));
            opCodes.put("ASR",new Integer(2));
            opCodes.put("ROR",new Integer(3));
            
            this.armExceptions = new Vector();
            this.armRegisters = new Vector();
            this.immedTable = new Vector();
            this.bitList = new Vector();
            this.softVectorCode = new Vector();
            
            /**
             * Reset
             */
            softVectorCode.addElement("0xef000090");
            softVectorCode.addElement("0xe1a0e00f");
            softVectorCode.addElement("0xe89b8800");
            softVectorCode.addElement("0xef000080");
            
            /**
             * Undef
             */
            softVectorCode.addElement("0xef000091");
            softVectorCode.addElement("0xe1a0e00f");
            softVectorCode.addElement("0xe89b8800");
            softVectorCode.addElement("0xef000081");
            
            /**
             * SWI
             */
            softVectorCode.addElement("0xef000092");
            softVectorCode.addElement("0xe1a0e00f");
            softVectorCode.addElement("0xe89b8800");
            softVectorCode.addElement("0xef000082");
            
            /**
             * Prefetch Abort
             */
            softVectorCode.addElement("0xef000093");
            softVectorCode.addElement("0xe1a0e00f");
            softVectorCode.addElement("0xe89b8800");
            softVectorCode.addElement("0xef000083");
            
            /**
             * Data Abort
             */
            softVectorCode.addElement("0xef000094");
            softVectorCode.addElement("0xe1a0e00f");
            softVectorCode.addElement("0xe89b8800");
            softVectorCode.addElement("0xef000084");
            
            /**
             * Address Exception
             */
            softVectorCode.addElement("0xef000095");
            softVectorCode.addElement("0xe1a0e00f");
            softVectorCode.addElement("0xe89b8800");
            softVectorCode.addElement("0xef000085");
            
            /**
             * IRQ
             */
            softVectorCode.addElement("0xef000096");
            softVectorCode.addElement("0xe1a0e00f");
            softVectorCode.addElement("0xe89b8800");
            softVectorCode.addElement("0xef000086");
            
            /**
             * FIQ
             */
            softVectorCode.addElement("0xef000097");
            softVectorCode.addElement("0xe1a0e00f");
            softVectorCode.addElement("0xe89b8800");
            softVectorCode.addElement("0xef000087");
            
            /**
             * Error
             */
            softVectorCode.addElement("0xef000098");
            softVectorCode.addElement("0xe1a0e00f");
            softVectorCode.addElement("0xe89b8800");
            softVectorCode.addElement("0xef000088");
            
            /**
             * Default Handler 
             */
            softVectorCode.addElement("0xe1a0f00e");
            
            this.armIo = new MEM();
            this.armMmu = new MMU();
            this.armOp = new DACT();
            
            this.armCpu = new Hashtable();
            this.armMem = new Hashtable();
        
            this.osBlock = new Hashtable();
            this.machSettings = new Hashtable();
            
            osBlock.put("Time0",new Object()); //ARMword
            osBlock.put("ErrorP",new Object()); //ARMWord
            osBlock.put("ErrorNo",new Object()); //ARMword
            osBlock.put("FileTable",new Object()); //hashtable
            osBlock.put("FileFlags",new Object()); //vector
            osBlock.put("tempNames",new Object()); //vector
        }
        
        
        
        /* Gettors and Settors */
        public void SetPC(Long pc)
        {
            Vector regs = (Vector)this.armState.get("Registers");
            regs.setElementAt(pc,15);
            this.armState.put("Registers",regs);
        }
        
        public void SetDebug(boolean d)
        {
            this.debug = d;
        }
        
        public long GetPC()
        {
            Long pc = new Long(0);
            
            Vector regs = (Vector) this.armState.get("Registers");
            pc = (Long) regs.elementAt(15);
            
            return pc.longValue();
        }
        
        public int GetIoCycleDivisor()
        {
            Integer icd = new Integer(0);
            
            if(this.machSettings.containsKey("Io_Cycle_Divisor"))
            {
                icd = (Integer) this.machSettings.get("Io_Cycle_Divisor");
            }
            
            return icd.intValue();
        }
        
        public Hashtable GetState()
        {
            return this.armState;
        }
        
        /* resets */
        public boolean Reset()
        {
            return false;
        }

        public boolean Reset(int obType)
        {
            boolean done = false;
            
            switch(obType)
            {
                case islandProcessorEngine.PROC_STATE:
                    this.armState.put("NextInstr",new Integer(0));
                    this.armState.put("Emulate",new Integer(3));
                    break;
            }
            
            return done;
        }
        
        public boolean Reset(int obType, boolean canStepDisassemble)
        {
            boolean done = false;
            
            switch(obType)
            {
                case islandProcessorEngine.PROC_STATE:
                
                    // reset state
                    
                    this.armState.put("NextInstr",new Integer(0));
                    this.armState.put("Emulate",new Integer(3));
                    this.armState.put("Disassemble", new Boolean(canStepDisassemble));
                    
                    if(this.presetRegfile.elementAt(1)!=null)
                    {
                        Vector reg = (Vector) this.armState.get("Reg");
                        
                        if(reg!=null)
                        {
                            reg.setElementAt(this.presetRegfile.elementAt(1),1);
                        }
                        else
                        {
                            reg = new Vector();
                            reg.setElementAt(this.presetRegfile.elementAt(1),1);
                        }
                    }
                    
                    break;
            }
            
            return done;
        }
        
        public void Io_Reset(Hashtable state)
        {
            
        }
        
        /* inits */
        public boolean Init() // EmuInit
        {
            boolean done = emulInit();
            
            return done;
        }
        
        public boolean Init(int obType)
        {
            boolean done = false;
            switch(obType)
            {
                case islandProcessorEngine.PROC_STATE :
                    this.Init();
                    this.createNewState();
                    this.armState.put("Cpu",this.armCpu);
                    this.armState.put("Mem_Bank", this.armMem);
                    this.armState.put("Abort_Model",new Integer(0));
                    
                    if(this.bigEndian)
                    {
                        this.armState.put("BigEndSig",new Integer(HIGH));
                    }
                    else
                    {
                        this.armState.put("BigEndSig",new Integer(LOW));
                    }
                    
                    // Memory Init
                    this.armMem = new Hashtable();
                    this.armIo.Init();
                    
                    // Os Init
                    this.osInit();
                    
                    this.armState.put("Verbose",new Integer(verbosity));
                    this.machSettings.put("Io_Cycle_Divisor",new Integer(50));
                    
                    break;
            }
            
            return done;
        }
        
        /* step num */
        public void StepOnce()
        {
            this.armState.put("NextInstr","RESUME");
            Vector regs = (Vector)this.armState.get("Registers");
            Long word = doProg(this.armState);
            regs.setElementAt(word,15);
        }
        
        /* reads */  
        public int ReadByte(short addr)
        {
            return 0;
        }            
        
        public int IceReadByte(Long addr, long pv)
        {
            return bus_read(8,addr,pv);
        }
        
        /* writes */
        public int WriteByte(short addr, int data)
        {
            return 0;
        }
        
        public int IceWriteByte(Long addr, long v)
        {
            return bus_write(8,addr,v);
        }
        
        /* parse functions */
        public int ParseCPU(String param)
        {
            return 0;
        }
        
        public int ParseMach(String param)
        {
            return 0;
        }
        
        public int ParseMem(int numParams, String param)
        {
            return 0;
        }
        
        public int ParseReg(int numParams, String param)
        {
            return 0;
        }
        
        // pprivate functions
        private void createNewState()
        {
            // initialize
            armState = new Hashtable();
            armState.put("Emulate",new Object());
            armState.put("EndCondition",new Object());
            armState.put("ErrorCode",new Object());
            armState.put("Registers",new Object());
            armState.put("CPSR",new Object());
            armState.put("SPSRs",new Object());
            armState.put("Accumulator",new Object());
            armState.put("NFlag",new Object());
            armState.put("ZFlag",new Object());
            armState.put("CFlag",new Object());
            armState.put("VFlag",new Object());
            armState.put("IFFlags",new Object());
            armState.put("TFlag",new Object());
            armState.put("Bank",new Object());
            armState.put("Mode",new Object());
            armState.put("Emulate",new Object());
            armState.put("Instr",new Object());
            armState.put("PC",new Object());
            armState.put("Temp",new Object());
            armState.put("Loaded",new Object());
            armState.put("Decoded",new Object());
            armState.put("Loaded_Addr",new Object());
            armState.put("Decoded_Addr",new Object());
            armState.put("NumSCycles",new Object());
            armState.put("NumNCycles",new Object());
            armState.put("NumICycles",new Object());
            armState.put("NumCCycles",new Object());
            armState.put("NumFCycles",new Object());
            armState.put("NumInstructions",new Object());
            armState.put("VectorCatch",new Object());
            armState.put("CallDebug",new Object());
            armState.put("CanWatch",new Object());
            armState.put("MemReadDebug",new Object());
            armState.put("MemWriteDebug",new Object());
            armState.put("StopHandle",new Object());
            armState.put("MemInPtr",new Object());
            armState.put("MemOutPtr",new Object());
            armState.put("MemSparePtr",new Object());
            armState.put("MemSize",new Object());
            armState.put("OSPtr",new Object());
            armState.put("CommandLine",new Object());
            armState.put("CP_Init",new Object());
            armState.put("CP_Exit",new Object());
            armState.put("LDCs",new Object());
            armState.put("STCs",new Object());
            armState.put("MRCs",new Object());
            armState.put("MCRs",new Object());
            armState.put("CDPs",new Object());
            armState.put("CPRead",new Object());
            armState.put("CPData",new Object());
            armState.put("CPRegWords",new Object());
            armState.put("EventSet",new Object());
            armState.put("Now",new Object());
            armState.put("EventPtr",new Object());
            armState.put("Debug",new Object());
            armState.put("N_ResetSig",new Object());
            armState.put("N_FiqSig",new Object());
            armState.put("N_IrqSig",new Object());
            armState.put("AbortSig",new Object());
            armState.put("N_TransSig",new Object());
            armState.put("BigEndSig",new Object());
            armState.put("Prog32Sig",new Object());
            armState.put("Data32Sig",new Object());
            armState.put("LateAbtSig",new Object());
            armState.put("Vector",new Object());
            armState.put("Aborted",new Object());
            armState.put("Reseted",new Object());
            armState.put("Inted",new Object());
            armState.put("LastInted",new Object());
            armState.put("Base",new Object());
            armState.put("AbortAddr",new Object());
            armState.put("DbgHostInterface",new Object());
            armState.put("Verbose",new Object());
            armState.put("MMU_State",new Object());
            armState.put("IsV6",new Object());
            armState.put("LastTime",new Object());
            armState.put("CP14R0_CCD",new Object());
            armState.put("Mach_IO",new Object());
            armState.put("Energy",new Object());
            armState.put("Dissassemble",new Object());
            armState.put("Tb_Now",new Object());
            armState.put("RegValue",new Object());
            armState.put("CPU_Config",new Object());
            armState.put("Mem_Config",new Object());
            armState.put("Mem_Bank",new Object());
            armState.put("Vector_Remap_Flag",new Object());
            armState.put("Vector_Remap_Addr",new Object());
            armState.put("Vector_Remap_Size",new Object());
            armState.put("NextInstr", new Object());
        }
        
        private void emulNewState()
        {
            
        }
        
        private boolean emulInit()
        {
            boolean done = false;
            long i,j;
            
            try
            {
                for(i =0; i<4096; i++) /* values of 12 bit dp rhs's */
                {
                    this.immedTable.setElementAt(this.rotater(i & 0xffL, (i >> 7L) & 0x1eL),(int)i); 
                }
                
                for(i=0; i<256; i++)
                {
                    this.bitList.setElementAt(new Integer(0),(int)i);
                }
                
                for(j = 1; j < 256; j <<=1)
                {
                    for(i =0; i < 256; i++)
                    {   
                        if((i & j)>0)
                        {
                            Integer val = (Integer)bitList.elementAt((int)i);
                            int valu = val.intValue();
                            valu++;
                            this.bitList.setElementAt(new Integer(valu),(int)i);
                        }
                    }
                }
                
                for(i =0; i<256; i++)
                {   
                    Integer v = (Integer) this.bitList.elementAt((int)i);
                    int vv = v.intValue();
                    vv*=4;
                    this.bitList.setElementAt(new Integer(vv),(int)i);
                }
            }
            catch(Exception ex)
            {
                System.out.println("[ERROR] problems starting arm emulator Init():"+ex.toString());
            }
            
            return done;
        }
        
        private void resetState()
        {
            
        }
        
        private Long rotater(long n, long b) // long == unsigned int
        {
            return new Long(((n) >> (b)) | ((n) << (32 - (b))));
        }
        
        private Long osInit()
        {
            long instr, i, j;
            
            try
            {
                if(armState.get("OSPtr") == null)
                {
                    armState.put("OSPtr", this.osBlock);
                }
                
                Hashtable OSptr = (Hashtable) armState.get("OSPtr");
                
                OSptr.put("ErrorP",new Integer(0));
                Vector regs = new Vector();
                regs.setElementAt("ADDRSUPERSTACK",13);
                armState.put("Registers",regs);
            
                setReg(armState, SVC32MODE,13, "ADDRSUPERSTACK");
                setReg(armState, ABORT32MODE,13, "ADDRSUPERSTACK");
                setReg(armState, UNDEF32MODE,13, "ADDRSUPERSTACK");
            }
            catch(Exception ex)
            {
                System.out.println("[ERROR] problems initializing virtual os:"+ex.toString());
            }
            
            return new Long(1);
        }
        
        private void setReg(Hashtable state, int mode, int regis, String stack)
        {
            
        }
        
        private Long doProg(Hashtable state)
        { 
            Long pc = new Long(0);
            
            
             
            return new Long(0);
        }
        
        private Long doFunct(Hashtable state)
        {
            Long pc = new Long(0);
            
            return new Long(0);
        }
        
        private Long doInstr(Hashtable state)
        {
            Long pc = new Long(0);
            
            return new Long(0);
        }
        
        private int bus_write(int size, Long addr, long data)
        {
            return 0;
        }
        
        private int bus_read(int size, Long addr, long data)
        {
            return 0;
        }
    }
    
    public class x86_Arch
    {
        
    }
    
    public class SPARC_Arch
    {
        
    }
    
    
    public class PPC_Arch
    {
        
    
    }
    
    public class MIPS_Arch
    {
        
        
    }
    
    
    public class BFIN_Arch
    {
        
       
    }
    
    public class COLDFIRE_Arch
    {
        
        
    }
    
    public class M86_Arch
    {
        
    
    }
    
    /**
     * Generic Processor Emulation. Set the Arch
     * Later. Initialze variables and create objects.
     */
    public islandProcessorEngine() 
    { 
        // default to dact
        this.no_dbct = true;
        
        // init custom objects
        config = new MachineConfig();
        config.UsesDBCT(no_dbct);
        
        codeCov = new Hashtable();
            
        codeCov.put("Prof_On","");
        codeCov.put("Start","");
        codeCov.put("End","");
        
        // initialize mapping container
        this.procMap = new Hashtable();
        
        // initialize state
        procEngineState = islandProcessorEngine.PROC_MACH_INITIALIZED;
        
        // initialize status
        procEngineStatus = islandProcessorEngine.WAIT_FOR_PROCESSOR_POINTS;
    }
    
    /**
     * Specific Processor Engine.
     * @param arch <description>
     */
    public islandProcessorEngine(int arch)
    {   
        // default to dact
        no_dbct = true;
        
        // set general configuration
        config = new MachineConfig();
        config.UsesDBCT(no_dbct);
        
        codeCov = new Hashtable();
            
        codeCov.put("Prof_On","");
        codeCov.put("Start","");
        codeCov.put("End","");
        
        // initialize mapping container
        this.procMap = new Hashtable();
        
        // initialize correct Arch obj
        config.InitArch(arch);
        
        // initialize call stack
        if(peCallStack == null)
        { 
            peCallStack = new Stack();
        }
        
        // initialize state
        procEngineState = islandProcessorEngine.PROC_MACH_INITIALIZED;
        
        // initialize status
        procEngineStatus = islandProcessorEngine.WAIT_FOR_PROCESSOR_POINTS;
    }
    
    /**
     * Specific Processor Engine.
     * @param arch <description>
     */
    public islandProcessorEngine(int arch, boolean mmuFlag)
    {
        // default to dact
        this.no_dbct = true;
        
        // set general configuration
        config = new MachineConfig();
        config.UsesMMU(mmuFlag);
        config.UsesDBCT(no_dbct);
        
        codeCov = new Hashtable();
            
        codeCov.put("Prof_On","");
        codeCov.put("Start","");
        codeCov.put("End","");
        
        // initialize mapping container
        this.procMap = new Hashtable();
        
        // initialize correct Arch obj
        config.InitArch(arch);
        
        // initialie call stack 
         if(peCallStack == null)
        { 
            peCallStack = new Stack();
        }
        
        // initialize state
        procEngineState = islandProcessorEngine.PROC_MACH_INITIALIZED;
        
        // initialize status
        procEngineStatus = islandProcessorEngine.WAIT_FOR_PROCESSOR_POINTS;
        
    }
    
    /**
     * Specific Processor Engine.
     * @param arch <description>
     */
    public islandProcessorEngine(int arch, boolean mmuFlag, String startAddress)
    {
        // set operation format flag
        this.no_dbct = true;
        
        // set general configuration
        config = new MachineConfig(startAddress);
        config.UsesMMU(mmuFlag);
        config.UsesDBCT(no_dbct);
        
        codeCov = new Hashtable();
        codeCov.put("Prof_On","");
        codeCov.put("Start","");
        codeCov.put("End","");
        
        // initialize mapping container
        this.procMap = new Hashtable();
        
        // initialize correct Arch obj
        config.InitArch(arch);
        
        // initialize call stack 
        if(peCallStack == null)
        { 
            peCallStack = new Stack();
        }
        
        // initialize state
        procEngineState = islandProcessorEngine.PROC_MACH_INITIALIZED;
        
        // initialize status
        procEngineStatus = islandProcessorEngine.WAIT_FOR_PROCESSOR_POINTS;
        
    }
    
    /**
     * Specific Processor Engine.
     * @param arch <description>
     */
    public islandProcessorEngine(MachineConfig proc_config)
    {
        // set operation format flag
        this.no_dbct = true;
        
        // set general configuration
        config = proc_config;
        
        codeCov = new Hashtable();
            
        codeCov.put("Prof_On","");
        codeCov.put("Start","");
        codeCov.put("End","");
        
        // initialize mapping container
        this.procMap = new Hashtable();
        
        // initialize necessary memory
        
        // initialie call stack 
        if(peCallStack == null)
        { 
            peCallStack = new Stack();
        }
        
        // initialize state
        procEngineState = islandProcessorEngine.PROC_MACH_INITIALIZED;
        
        // initialize status
        procEngineStatus = islandProcessorEngine.WAIT_FOR_PROCESSOR_POINTS;
        
    }
    
    
    /* engine functions */
    public int GetEngineState()
    {
        return procEngineState;
    }
    
    public int GetEngineStatus()
    {
       return procEngineStatus; 
    }
    
    public boolean StartEngine()
    {
        // finish machine config setup
        
        // finish machine setup
        
        // begin at start address
        
        return false;
    }
    
    public boolean StopEngine()
    {
        // shut down machine
        
        // kill app loops
        
        return false;
    }
    
    /* Kernel functions */
    public int GetKernelFunction()
    {
        return 0;
    }
    
    public int SetKernelInterrupt()
    {
        return 0;
    }
    
    /* program specific */
    public void SetProgramOffset(String offsetAddress)
    {
        this.offset = offsetAddress;
        
        // somehow set the int offset
    }
    
    public void SetProgramInstruction(String instructionAddress, String instruction)
    {
        DACT assmInstruction;
        DBCT binInstruction;
        Object instr;
        
        // Set operation translation object
        if(this.no_dbct)
        {
            assmInstruction = new DACT(instruction);
            
            // Add to actual operations collection
            peInstructions.put(instructionAddress,assmInstruction);
            
            instr = (Object)  assmInstruction;
        }
        else
        {
            binInstruction = new DBCT(instruction);
            
             // Add to actual operations collection
            peInstructions.put(instructionAddress, binInstruction);
            
            instr = (Object)  binInstruction;
        }
        
        
        
        this.procMap.put(instructionAddress,instr);
    }
    
    public void SetProgramJumpInstruction(String instructionAddress, String instruction)
    {
        
    }
    
    public void SetProgramJumpLinkInstruction(String instructionAddress, String instruction)
    {
        
    }
    
    public void SetProgramStackInstruction(String instructionAddress, String instruction)
    {
        
    }
    
    /* Machine Specific */
    public void InitMachine(boolean useDbct)
    {
        
    }
    
    public void InitMachine(boolean useDbct, boolean debug)
    {
        
    }

    public void InitMachine(boolean useDbct, boolean debug, int type)
    {
        
    }
    
    public int GetMachineStatus()
    {
        return 0;
    }
    
    public String GetMachineName()
    {
        return this.config.GetName();
    }
    
    public boolean CyclingToJump()
    {
        return false;
    }
        
    public boolean CyclingToStackEvent()
    {
        return false;
    }

    public boolean CyclingToValue()
    {
        return false;
    }
    
    public boolean  CyclingToInterrupt()
    {
        return false;
    }
    
    public int CycleToNextEventFromAddress(String sAddress)
    {
        return 0;
    }
    
    public int CycleToEndAddressFromAddress(String sAddress,String eAddress)
    {
        return 0;
    }
    
    /* Stack interaction functions */
    public void SetStackFunctionForExecution(String function)
    {
        // setup stack element
        
        // Add element(s) to actual stack
        
    }
    
    public void SetStackAddressForExecution(String address)
    {
        // setup stack element
        
        // Add element(s) to actual stack
        
    }
    
    public Object GetStackValueForFunction(String function)
    {
        return new Object();
    }
    
    public Object GetStackValueForAddress(String address)
    {
        return new Object();
    }
    
} 