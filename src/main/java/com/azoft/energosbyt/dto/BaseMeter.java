package com.azoft.energosbyt.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class BaseMeter {
    Srch srch ;
    Srch_res srch_res;
    public Srch getSrch() {
        if (srch==null) srch = new Srch ();
        return srch;
    }
    public void setSrch(Srch srch) {
        this.srch = srch;
    }
    public Srch_res getSrch_res() {
        if (srch_res==null) srch_res = new  Srch_res();
        return srch_res;
    }
    public void setSrch_res(Srch_res srch_res) {
        this.srch_res = srch_res;
    }
    public class Srch{
        String person_Id;
        String account_Number;
        String contract_Number;
        String premise_Id;
        String account_Id;
        public String getPerson_Id() {
            return person_Id;
        }
        public void setPerson_Id(String person_Id) {
            this.person_Id = person_Id;
        }
        public String getAccount_Number() {
            return account_Number;
        }
        public void setAccount_Number(String account_Number) {
            this.account_Number = account_Number;
        }
        public String getContract_Number() {
            return contract_Number;
        }
        public void setContract_Number(String contract_Number) {
            this.contract_Number = contract_Number;
        }
        public String getPremise_Id() {
            return premise_Id;
        }
        public void setPremise_Id(String premise_Id) {
            this.premise_Id = premise_Id;
        }
        public String getAccount_Id() {
            return account_Id;
        }
        public void setAccount_Id(String account_Id) {
            this.account_Id = account_Id;
        }

    }
    public static class  Srch_res{
        List <Srch_res_s> serv ;
        public List<Srch_res_s> getServ() {
            if (serv == null) serv = new ArrayList<Srch_res_s>();
            return serv;
        }
        public void setServ(List<Srch_res_s> serv) {
            this.serv = serv;
        }
        public static  class Srch_res_s{
            String account_id;
            String serviceType;

            public static class Service_point{
                String premise_id;
                String sp_id ;
                String serviceType;
                public  static class Conn_history{
                    Date start_date;
                    Date stop_date;
                    String SaSp;
                    String hou;
                    String percent;
                    public Date getStart_date() {
                        return start_date;
                    }
                    public void setStart_date(Date start_date) {
                        this.start_date = start_date;
                    }
                    public Date getStop_date() {
                        return stop_date;
                    }
                    public void setStop_date(Date stop_date) {
                        this.stop_date = stop_date;
                    }
                    public String getSaSp() {
                        return SaSp;
                    }
                    public void setSaSp(String saSp) {
                        SaSp = saSp;
                    }
                    public String getHou() {
                        return hou;
                    }
                    public void setHou(String hou) {
                        this.hou = hou;
                    }
                    public String getPercent() {
                        return percent;
                    }
                    public void setPercent(String percent) {
                        this.percent = percent;
                    }
                }
                List <Conn_history> CHs;
                public static class Sp_history{
                    Date install_date;
                    Date remove_date;
                    String meter_id;
                    String m_const;
                    public Date getInstall_date() {
                        return install_date;
                    }
                    public void setInstall_date(Date install_date) {
                        this.install_date = install_date;
                    }
                    public Date getRemove_date() {
                        return remove_date;
                    }
                    public void setRemove_date(Date remove_date) {
                        this.remove_date = remove_date;
                    }
                    public String getMeter_id() {
                        return meter_id;
                    }
                    public void setMeter_id(String meter_id) {
                        this.meter_id = meter_id;
                    }
                    public String getM_const() {
                        return m_const;
                    }
                    public void setM_const(String m_const) {
                        this.m_const = m_const;
                    }
                }
                List <Sp_history> SPHs;
                public String getPremise_id() {
                    return premise_id;
                }
                public void setPremise_id(String premise_id) {
                    this.premise_id = premise_id;
                }
                public String getServiceType() {
                    return serviceType;
                }
                public void setServiceType(String serviceType) {
                    this.serviceType = serviceType;
                }
                public String getSp_id() {
                    return sp_id;
                }
                public void setSp_id(String sp_id) {
                    this.sp_id = sp_id;
                }
                public List<Conn_history> getCHs() {
                    if (CHs==null) CHs= new ArrayList<Conn_history>();
                    return CHs;
                }
                public void setCHs(List<Conn_history> cHs) {
                    CHs = cHs;
                }
                public List<Sp_history> getSPHs() {
                    if (SPHs==null) SPHs = new ArrayList<Sp_history>();
                    return SPHs;
                }
                public void setSPHs(List<Sp_history> sPHs) {
                    SPHs = sPHs;
                }
            };
            List<Service_point> SPs;
            public String getAccount_id() {
                return account_id;
            }
            public void setAccount_id(String account_id) {
                this.account_id = account_id;
            }
            public String getServiceType() {
                return serviceType;
            }
            public void setServiceType(String serviceType) {
                this.serviceType = serviceType;
            }
            public List<Service_point> getSPs() {
                if (SPs==null) SPs = new ArrayList<Service_point>();
                return SPs;
            }
            public void setSPs(List<Service_point> sPs) {
                SPs = sPs;
            }
        }
    }


    String error_code;
    String error_message;

    String system_id;
    String action;
    String code;
    String filial_code;
    String id;
    Date allow_expl_date ;
    Date check_date ;
    int check_period ;
    Date remove_date ;

    String load_type;
    String number;
    String position_id;
    String state;
    String type;
    String type_desc;


    String badgeNumber;
    String meterType ;
    String meterType_desc  ;
    String meterStatus;
    String manufacturer;
    String manufacturer_desc;
    String model;
    String model_desc;
    String serialNumber;
    String receivedDate ;
    String longDescription ;
    String meterCharacteristic;
    String directionMeasurement	;
    String ratedCurrent	;
    String countingMechanism;
    String ownerMeter;
    String ownerMeter_desc;
    String ratedVoltageLinear;
    String accuracyClass	;
    String diameter;
    String sealingDate;
    String actNumber;
    String faultyMeter;
    String typeWaterMeters;
    String reFailure;
    String calibrationInterval;
    String ratedVoltagePhase;
    String sealNumber;
    String reprogrammingNumber;
    String verificationDate;
    String typeAct;
    String typeMeasuredEnergy;
    String maximumCurrent;
    String life;
    String releaseMeterDate;
    String meterConfig;
    String meterConfigurationId;
    String effectiveDateTime;

    List<Registr> registers = new ArrayList<Registr>();
    public static class Registr {
        String registerId;
        String seq;
        String unitOfMeasure;
        String timeOfUse;
        String registerConstant;
        String consumptionType;
        String howToUse;
        String numberOfDigitsLeft;
        String numberOfDigitsRight;
        String fullScale;
        String readOutType;
        String intervalRegisterType;

        public Registr() {
            super();
        }

        @JsonIgnore
        public Registr(String registerId,String seq,String unitOfMeasure,String timeOfUse,String registerConstant,String consumptionType,
                       String howToUse,String numberOfDigitsLeft,String numberOfDigitsRight,String fullScale,String readOutType,
                       String intervalRegisterType){
            this.registerId = registerId;
            this.seq = seq;
            this.unitOfMeasure  = unitOfMeasure;
            this.timeOfUse = timeOfUse;
            this.registerConstant = registerConstant;
            this.consumptionType = consumptionType;
            this.howToUse = howToUse;
            this.numberOfDigitsLeft = numberOfDigitsLeft;
            this.numberOfDigitsRight = numberOfDigitsRight;
            this.fullScale = fullScale;
            this.readOutType = readOutType;
            this.intervalRegisterType = intervalRegisterType;
        }


        public String getRegisterId() {
            return registerId;
        }

        public void setRegisterId(String registerId) {
            this.registerId = registerId;
        }

        public String getSeq() {
            return seq;
        }

        public void setSeq(String seq) {
            this.seq = seq;
        }

        public String getUnitOfMeasure() {
            return unitOfMeasure;
        }

        public void setUnitOfMeasure(String unitOfMeasure) {
            this.unitOfMeasure = unitOfMeasure;
        }

        public String getTimeOfUse() {
            return timeOfUse;
        }

        public void setTimeOfUse(String timeOfUse) {
            this.timeOfUse = timeOfUse;
        }

        public String getRegisterConstant() {
            return registerConstant;
        }

        public void setRegisterConstant(String registerConstant) {
            this.registerConstant = registerConstant;
        }

        public String getConsumptionType() {
            return consumptionType;
        }

        public void setConsumptionType(String consumptionType) {
            this.consumptionType = consumptionType;
        }

        public String getHowToUse() {
            return howToUse;
        }

        public void setHowToUse(String howToUse) {
            this.howToUse = howToUse;
        }

        public String getNumberOfDigitsLeft() {
            return numberOfDigitsLeft;
        }

        public void setNumberOfDigitsLeft(String numberOfDigitsLeft) {
            this.numberOfDigitsLeft = numberOfDigitsLeft;
        }

        public String getNumberOfDigitsRight() {
            return numberOfDigitsRight;
        }

        public void setNumberOfDigitsRight(String numberOfDigitsRight) {
            this.numberOfDigitsRight = numberOfDigitsRight;
        }

        public String getFullScale() {
            return fullScale;
        }

        public void setFullScale(String fullScale) {
            this.fullScale = fullScale;
        }

        public String getReadOutType() {
            return readOutType;
        }

        public void setReadOutType(String readOutType) {
            this.readOutType = readOutType;
        }

        public String getIntervalRegisterType() {
            return intervalRegisterType;
        }

        public void setIntervalRegisterType(String intervalRegisterType) {
            this.intervalRegisterType = intervalRegisterType;
        }



    }

    public List<Registr> getRegisters() {
        if (registers==null) registers = new ArrayList<Registr>();
        return registers;
    }
    public void setRegisters(List<Registr> registers) {
        this.registers = registers;
    }

    public String getError_code() {
        return error_code;
    }
    public void setError_code(String error_code) {
        this.error_code = error_code;
    }
    public String getError_message() {
        return error_message;
    }
    public void setError_message(String error_message) {
        this.error_message = error_message;
    }

    public String getBadgeNumber() {
        return badgeNumber;
    }
    public void setBadgeNumber(String badgeNumber) {
        this.badgeNumber = badgeNumber;
    }
    public String getMeterType() {
        return meterType;
    }
    public void setMeterType(String meterType) {
        this.meterType = meterType;
    }
    public String getMeterStatus() {
        return meterStatus;
    }
    public void setMeterStatus(String meterStatus) {
        this.meterStatus = meterStatus;
    }
    public String getManufacturer() {
        return manufacturer;
    }
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public String getSerialNumber() {
        return serialNumber;
    }
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    public String getReceivedDate() {
        return receivedDate;
    }
    public void setReceivedDate(String receivedDate) {
        this.receivedDate = receivedDate;
    }
    public String getLongDescription() {
        return longDescription;
    }
    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }
    public String getMeterCharacteristic() {
        return meterCharacteristic;
    }
    public void setMeterCharacteristic(String meterCharacteristic) {
        this.meterCharacteristic = meterCharacteristic;
    }
    public String getDirectionMeasurement() {
        return directionMeasurement;
    }
    public void setDirectionMeasurement(String directionMeasurement) {
        this.directionMeasurement = directionMeasurement;
    }
    public String getRatedCurrent() {
        return ratedCurrent;
    }
    public void setRatedCurrent(String ratedCurrent) {
        this.ratedCurrent = ratedCurrent;
    }
    public String getCountingMechanism() {
        return countingMechanism;
    }
    public void setCountingMechanism(String countingMechanism) {
        this.countingMechanism = countingMechanism;
    }
    public String getOwnerMeter() {
        return ownerMeter;
    }
    public void setOwnerMeter(String ownerMeter) {
        this.ownerMeter = ownerMeter;
    }
    public String getRatedVoltageLinear() {
        return ratedVoltageLinear;
    }
    public void setRatedVoltageLinear(String ratedVoltageLinear) {
        this.ratedVoltageLinear = ratedVoltageLinear;
    }
    public String getAccuracyClass() {
        return accuracyClass;
    }
    public void setAccuracyClass(String accuracyClass) {
        this.accuracyClass = accuracyClass;
    }
    public String getDiameter() {
        return diameter;
    }
    public void setDiameter(String diameter) {
        this.diameter = diameter;
    }
    public String getSealingDate() {
        return sealingDate;
    }
    public void setSealingDate(String sealingDate) {
        this.sealingDate = sealingDate;
    }
    public String getActNumber() {
        return actNumber;
    }
    public void setActNumber(String actNumber) {
        this.actNumber = actNumber;
    }
    public String getFaultyMeter() {
        return faultyMeter;
    }
    public void setFaultyMeter(String faultyMeter) {
        this.faultyMeter = faultyMeter;
    }
    public String getTypeWaterMeters() {
        return typeWaterMeters;
    }
    public void setTypeWaterMeters(String typeWaterMeters) {
        this.typeWaterMeters = typeWaterMeters;
    }
    public String getReFailure() {
        return reFailure;
    }
    public void setReFailure(String reFailure) {
        this.reFailure = reFailure;
    }
    public String getCalibrationInterval() {
        return calibrationInterval;
    }
    public void setCalibrationInterval(String calibrationInterval) {
        this.calibrationInterval = calibrationInterval;
    }
    public String getRatedVoltagePhase() {
        return ratedVoltagePhase;
    }
    public void setRatedVoltagePhase(String ratedVoltagePhase) {
        this.ratedVoltagePhase = ratedVoltagePhase;
    }
    public String getSealNumber() {
        return sealNumber;
    }
    public void setSealNumber(String sealNumber) {
        this.sealNumber = sealNumber;
    }
    public String getReprogrammingNumber() {
        return reprogrammingNumber;
    }
    public void setReprogrammingNumber(String reprogrammingNumber) {
        this.reprogrammingNumber = reprogrammingNumber;
    }
    public String getVerificationDate() {
        return verificationDate;
    }
    public void setVerificationDate(String verificationDate) {
        this.verificationDate = verificationDate;
    }
    public String getTypeAct() {
        return typeAct;
    }
    public void setTypeAct(String typeAct) {
        this.typeAct = typeAct;
    }
    public String getTypeMeasuredEnergy() {
        return typeMeasuredEnergy;
    }
    public void setTypeMeasuredEnergy(String typeMeasuredEnergy) {
        this.typeMeasuredEnergy = typeMeasuredEnergy;
    }
    public String getMaximumCurrent() {
        return maximumCurrent;
    }
    public void setMaximumCurrent(String maximumCurrent) {
        this.maximumCurrent = maximumCurrent;
    }
    public String getLife() {
        return life;
    }
    public void setLife(String life) {
        this.life = life;
    }
    public String getReleaseMeterDate() {
        return releaseMeterDate;
    }
    public void setReleaseMeterDate(String releaseMeterDate) {
        this.releaseMeterDate = releaseMeterDate;
    }
    public String getMeterConfig() {
        return meterConfig;
    }
    public void setMeterConfig(String meterConfig) {
        this.meterConfig = meterConfig;
    }
    public String getMeterConfigurationId() {
        return meterConfigurationId;
    }
    public void setMeterConfigurationId(String meterConfigurationId) {
        this.meterConfigurationId = meterConfigurationId;
    }
    public String getEffectiveDateTime() {
        return effectiveDateTime;
    }
    public void setEffectiveDateTime(String effectiveDateTime) {
        this.effectiveDateTime = effectiveDateTime;
    }



    public Date getAllow_expl_date() {
        return allow_expl_date;
    }
    public void setAllow_expl_date(Date allow_expl_date) {
        this.allow_expl_date = allow_expl_date;
    }
    public Date getCheck_date() {
        return check_date;
    }
    public void setCheck_date(Date check_date) {
        this.check_date = check_date;
    }
    public int getCheck_period() {
        return check_period;
    }
    public void setCheck_period(int check_period) {
        this.check_period = check_period;
    }
    public Date getRemove_date() {
        return remove_date;
    }
    public void setRemove_date(Date remove_date) {
        this.remove_date = remove_date;
    }

    public String getSystem_id() {
        return system_id;
    }
    public void setSystem_id(String system_id) {
        this.system_id = system_id;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getFilial_code() {
        return filial_code;
    }
    public void setFilial_code(String filial_code) {
        this.filial_code = filial_code;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getLoad_type() {
        return load_type;
    }
    public void setLoad_type(String load_type) {
        this.load_type = load_type;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getPosition_id() {
        return position_id;
    }
    public void setPosition_id(String position_id) {
        this.position_id = position_id;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getType_desc() {
        return type_desc;
    }
    public void setType_desc(String type_desc) {
        this.type_desc = type_desc;
    }
    public String getMeterType_desc() {
        return meterType_desc;
    }
    public void setMeterType_desc(String meterType_desc) {
        this.meterType_desc = meterType_desc;
    }
    public String getManufacturer_desc() {
        return manufacturer_desc;
    }
    public void setManufacturer_desc(String manufacturer_desc) {
        this.manufacturer_desc = manufacturer_desc;
    }
    public String getModel_desc() {
        return model_desc;
    }
    public void setModel_desc(String model_desc) {
        this.model_desc = model_desc;
    }
    public String getOwnerMeter_desc() {
        return ownerMeter_desc;
    }
    public void setOwnerMeter_desc(String ownerMeter_desc) {
        this.ownerMeter_desc = ownerMeter_desc;
    }



}
