package com.algaworks.algasensors.temperature.monitoring.domain.service.impl;

import com.algaworks.algasensors.temperature.monitoring.api.model.TemperatureLogData;
import com.algaworks.algasensors.temperature.monitoring.domain.model.SensorId;
import com.algaworks.algasensors.temperature.monitoring.domain.repository.SensorAlertRepository;
import com.algaworks.algasensors.temperature.monitoring.domain.service.SensorAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensorAlertServiceImpl implements SensorAlertService {
	
	private final SensorAlertRepository sensorAlertRepository;
	
	@Transactional
	@Override
	public void handleAlert(TemperatureLogData temperatureLogData) {
		sensorAlertRepository.findById(new SensorId(temperatureLogData.getSensorId()))
				.ifPresentOrElse(alert -> { //
					
					if (alert.getMaxTemperature() != null && temperatureLogData.getValue().compareTo(alert.getMaxTemperature()) >= 0) {
						log.info("Alert Max Temperature for SensorId: '{}', Temperature: '{}'", temperatureLogData.getSensorId(), temperatureLogData.getValue());
					} else if (alert.getMinTemperature() != null && temperatureLogData.getValue().compareTo(alert.getMinTemperature()) <= 0) {
						log.info("Alert Min Temperature for SensorId: '{}', Temperature: '{}'", temperatureLogData.getSensorId(), temperatureLogData.getValue());
					} else {
						logAlertIgnored(temperatureLogData);
					}
					
				}, () -> logAlertIgnored(temperatureLogData));
	}
	
	private static void logAlertIgnored(TemperatureLogData temperatureLogData) {
		log.info("Alert Ignored for SensorId: '{}', Temperature: '{}'", temperatureLogData.getSensorId(), temperatureLogData.getValue());
	}
}
