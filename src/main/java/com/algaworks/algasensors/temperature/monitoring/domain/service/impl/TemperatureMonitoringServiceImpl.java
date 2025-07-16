package com.algaworks.algasensors.temperature.monitoring.domain.service.impl;

import com.algaworks.algasensors.temperature.monitoring.api.model.TemperatureLogData;
import com.algaworks.algasensors.temperature.monitoring.domain.model.SensorId;
import com.algaworks.algasensors.temperature.monitoring.domain.model.SensorMonitoring;
import com.algaworks.algasensors.temperature.monitoring.domain.model.TemperatureLog;
import com.algaworks.algasensors.temperature.monitoring.domain.model.TemperatureLogId;
import com.algaworks.algasensors.temperature.monitoring.domain.repository.SensorMonitoringRepository;
import com.algaworks.algasensors.temperature.monitoring.domain.repository.TemperatureLogRepository;
import com.algaworks.algasensors.temperature.monitoring.domain.service.TemperatureMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class TemperatureMonitoringServiceImpl implements TemperatureMonitoringService {
	
	private final SensorMonitoringRepository sensorMonitoringRepository;
	private final TemperatureLogRepository temperatureLogRepository;
	
	@Override
	@Transactional
	public void processTemperatureReading(TemperatureLogData temperatureLogData) {
		sensorMonitoringRepository.findById(new SensorId(temperatureLogData.getSensorId()))
				.ifPresentOrElse(
						sensor -> handleSensorMonitoring(temperatureLogData, sensor),
						() -> logTemperatureIgnore(temperatureLogData)
				);
	}
	
	private void logTemperatureIgnore(TemperatureLogData temperatureLogData) {
		log.warn("Temperature ignored: SensorId: '{}', Temperature: '{}'", temperatureLogData.getSensorId(), temperatureLogData.getValue());
	}
	
	private void handleSensorMonitoring(TemperatureLogData temperatureLogData, SensorMonitoring sensor) {
		if (sensor.isEnabled()) {
			sensor.setLastTemperature(temperatureLogData.getValue());
			sensor.setUpdatedAt(OffsetDateTime.now());
			sensorMonitoringRepository.save(sensor);
			
			TemperatureLog temperatureLog = TemperatureLog.builder()
					.id(new TemperatureLogId(temperatureLogData.getId()))
					.registeredAt(temperatureLogData.getRegisteredAt())
					.value(temperatureLogData.getValue())
					.sensorId(new SensorId(temperatureLogData.getSensorId()))
					.build();
			
			temperatureLogRepository.save(temperatureLog);
			log.info("Temperature updated: SensorId: '{}', Temperature: '{}'", temperatureLogData.getSensorId(), temperatureLogData.getValue());
		} else {
			logTemperatureIgnore(temperatureLogData);
		}
	}
}
