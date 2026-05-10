package com.casino.ledger.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.Getter;

@Getter
public enum LedgerEntryType {
    PLAYER_DEPOSIT_FUNDS(1, "Player Deposit Funds"),
    PLAYER_WITHDRAW_FUNDS(2, "Player Withdraw Funds"),
    PLAYER_BET(3, "Player Bet"),
    PLAYER_LOSS(4, "Player Loss"),
    PLAYER_WIN(5, "Player Win");

    private final int code;
    private final String name;

    LedgerEntryType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static LedgerEntryType fromCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (LedgerEntryType value : values()) {
            if (value.code == code) {
                return value;
            }
        }

        throw new IllegalArgumentException("Unknown LedgerEntryType code: " + code);
    }

    @Converter
    public static class LedgerEntryTypeConverter implements AttributeConverter<LedgerEntryType, Integer> {

        @Override
        public Integer convertToDatabaseColumn(LedgerEntryType attribute) {
            return attribute == null ? null : attribute.getCode();
        }

        @Override
        public LedgerEntryType convertToEntityAttribute(Integer dbData) {
            return LedgerEntryType.fromCode(dbData);
        }
    }
}
