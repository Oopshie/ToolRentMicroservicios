package com.toolrent.ms_rate.Service;

import com.toolrent.ms_rate.Entity.RateEntity;
import com.toolrent.ms_rate.Repository.RateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RateService {
    @Autowired
    RateRepository rateRepository;

    public RateEntity getLatestRate() {
        return rateRepository.findTopByOrderByIdDesc();
    }

    public RateEntity createRate(int dailyRentalRate, int dailyLateFeeRent) {

        RateEntity rate = new RateEntity();
        rate.setDailyRentalRate(dailyRentalRate);
        rate.setDailyLateFeeRent(dailyLateFeeRent);

        return rateRepository.save(rate);
    }

    public RateEntity addRate(RateEntity rate) {
        return rateRepository.save(rate);
    }

    RateEntity findTopByOrderByIdDesc() {
        return rateRepository.findTopByOrderByIdDesc();
    }

}
