package org.example.service;

import org.example.entity.AddressDetails;
import java.util.List;

public interface AddressDetailsService {
    AddressDetails createAddress(AddressDetails address);
    AddressDetails getAddressById(Long id);
    List<AddressDetails> getAllAddresses();
    AddressDetails updateAddress(Long id, AddressDetails updated);
    void deleteAddress(Long id);
}
