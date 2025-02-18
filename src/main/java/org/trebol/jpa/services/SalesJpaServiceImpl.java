package org.trebol.jpa.services;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import javassist.NotFoundException;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.trebol.exceptions.EntityAlreadyExistsException;
import org.trebol.jpa.entities.*;
import org.trebol.jpa.repositories.*;
import org.trebol.pojo.*;
import org.trebol.exceptions.BadInputException;
import org.trebol.jpa.GenericJpaService;
import org.trebol.jpa.ISalesJpaService;

/**
 *
 * @author Benjamin La Madrid <bg.lamadrid at gmail.com>
 */
@Transactional
@Service
public class SalesJpaServiceImpl
  extends GenericJpaService<SellPojo, Sell>
  implements ISalesJpaService {

  private final ISalesJpaRepository salesRepository;
  private final ISellStatusesJpaRepository statusesRepository;
  private final IBillingTypesJpaRepository billingTypesRepository;
  private final IPaymentTypesJpaRepository paymentTypesRepository;
  private final IBillingCompaniesJpaRepository billingCompaniesRepository;
  private final IShippersJpaRepository shippersRepository;
  private final IAddressesJpaRepository addressesRepository;
  private final GenericJpaService<BillingCompanyPojo, BillingCompany> billingCompaniesService;
  private final GenericJpaService<CustomerPojo, Customer> customersService;
  private final GenericJpaService<SalespersonPojo, Salesperson> salespeopleService;
  private final ICustomersJpaRepository customersRepository;
  private final IProductsJpaRepository productsRepository;
  private final ConversionService conversion;
  private final Validator validator;

  @Autowired
  public SalesJpaServiceImpl(ISalesJpaRepository repository, ConversionService conversion,
                             ISellStatusesJpaRepository statusesRepository, IBillingTypesJpaRepository billingTypesRepository,
                             IBillingCompaniesJpaRepository billingCompaniesRepository, IPaymentTypesJpaRepository paymentTypesRepository,
                             IAddressesJpaRepository addressesRepository,
                             IShippersJpaRepository shippersRepository,
                             GenericJpaService<BillingCompanyPojo, BillingCompany> billingCompaniesService,
                             GenericJpaService<CustomerPojo, Customer> customersService,
                             GenericJpaService<SalespersonPojo, Salesperson> salespeopleService,
                             ICustomersJpaRepository customersRepository, IProductsJpaRepository productsRepository,
                             Validator validator) {
    super(repository, LoggerFactory.getLogger(SalesJpaServiceImpl.class));
    this.salesRepository = repository;
    this.conversion = conversion;
    this.statusesRepository = statusesRepository;
    this.billingTypesRepository = billingTypesRepository;
    this.billingCompaniesRepository = billingCompaniesRepository;
    this.paymentTypesRepository = paymentTypesRepository;
    this.addressesRepository = addressesRepository;
    this.shippersRepository = shippersRepository;
    this.billingCompaniesService = billingCompaniesService;
    this.customersService = customersService;
    this.salespeopleService = salespeopleService;
    this.customersRepository = customersRepository;
    this.productsRepository = productsRepository;
    this.validator = validator;
  }

  @Transactional
  @Override
  public SellPojo create(SellPojo inputPojo) throws BadInputException, EntityAlreadyExistsException {
    Sell input = this.convertToNewEntity(inputPojo);
    Sell output = repository.saveAndFlush(input);
    return this.convertToPojo(output);
  }

  @Override
  public SellPojo convertToPojo(Sell source) {
    // TODO can lesser null checks be used ?
    SellPojo target = conversion.convert(source, SellPojo.class);
    if (target != null) {

      target.setStatus(source.getStatus().getName());
      target.setPaymentType(source.getPaymentType().getName());
      target.setBillingType(source.getBillingType().getName());

      if (target.getBillingType().equals("Enterprise Invoice")) {
        BillingCompany sourceBillingCompany = source.getBillingCompany();
        if (sourceBillingCompany != null) {
          BillingCompanyPojo targetBillingCompany = conversion.convert(sourceBillingCompany, BillingCompanyPojo.class);
          target.setBillingCompany(targetBillingCompany);
        }
      }

      if (source.getBillingAddress() != null) {
        AddressPojo billingAddress = conversion.convert(source.getBillingAddress(), AddressPojo.class);
        target.setBillingAddress(billingAddress);
      }

      if (source.getShippingAddress() != null) {
        AddressPojo shippingAddress = conversion.convert(source.getShippingAddress(), AddressPojo.class);
        target.setShippingAddress(shippingAddress);
      }

      CustomerPojo customer = customersService.convertToPojo(source.getCustomer());
      target.setCustomer(customer);

      if (source.getSalesperson() != null) {
        SalespersonPojo salesperson = salespeopleService.convertToPojo(source.getSalesperson());
        target.setSalesperson(salesperson);
      }
    }
    return target;
  }

  @Transactional
  @Override
  public Sell convertToNewEntity(SellPojo source) throws BadInputException {
    Sell target = new Sell();

    if (source.getDate() != null) {
      target.setDate(source.getDate());
    }

    this.applyStatus(source, target);
    this.applyPaymentType(source, target);
    this.applyBillingTypeAndCompany(source, target);
    this.applyCustomer(source, target);
    this.applyBillingAddress(source, target);
    this.applyShippingAddress(source, target);
    // TODO uncomment when shipper API is included
    // this.applyShipper(source, target);
    this.applyDetails(source, target);

    return target;
  }

  @Transactional
  @Override
  public Sell applyChangesToExistingEntity(SellPojo source, Sell existing) throws BadInputException {
    Sell target = new Sell(existing);

    if (source.getDate() != null) {
      target.setDate(source.getDate());
    }
    if (source.getStatus() != null) {
      this.applyStatus(source, target);
    }
    if (source.getPaymentType() != null) {
      this.applyPaymentType(source, target);
    }
    if (source.getBillingType() != null) {
      this.applyBillingTypeAndCompany(source, target);
    }
    if (source.getCustomer() != null) {
      this.applyCustomer(source, target);
    }
    if (source.getBillingAddress() != null) {
      this.applyBillingAddress(source, target);
    }
    if (source.getShippingAddress() != null) {
      this.applyShippingAddress(source, target);
    }

    /*
    // TODO add shipper API
    if (source.getShipper() != null) {
      this.applyShipper(source, target);
    } */
    if (source.getDetails() != null) {
      this.applyDetails(source, target);
    }

    return target;
  }

  @Override
  public Optional<Sell> getExisting(SellPojo input) throws BadInputException {
    Long buyOrder = input.getBuyOrder();
    if (buyOrder == null) {
      throw new BadInputException("Invalid buy order.");
    } else {
      return this.salesRepository.findById(buyOrder);
    }
  }

  @Override
  public Predicate parsePredicate(Map<String, String> queryParamsMap) {
    QSell qSell = QSell.sell;
    BooleanBuilder predicate = new BooleanBuilder();
    for (String paramName : queryParamsMap.keySet()) {
      String stringValue = queryParamsMap.get(paramName);
      try {
        switch (paramName) {
          case "id":
            return predicate.and(qSell.id.eq(Long.valueOf(stringValue))); // match por id es único
          case "date":
            predicate.and(qSell.date.eq(Instant.parse(stringValue)));
            break;
          case "statusName":
            predicate.and(qSell.status.name.eq(stringValue));
            break;
          case "token":
            predicate.and(qSell.transactionToken.eq(stringValue));
            break;
          default:
            break;
        }
      } catch (NumberFormatException exc) {
        logger.info("Param '{}' couldn't be parsed as number (value: '{}')", paramName, stringValue);
      } catch (DateTimeParseException exc) {
        logger.warn("Param '{}' couldn't be parsed as date (value: '{}')", paramName, stringValue, exc);
      }
    }

    return predicate;
  }

  @Override
  public SellPojo readOne(Long id) throws NotFoundException {
    Optional<Sell> matchingSell = salesRepository.findById(id);
    if (matchingSell.isPresent()) {
      Sell found = matchingSell.get();
      SellPojo foundPojo = this.convertToPojo(found);
      this.applyDetails(found, foundPojo);
      return foundPojo;
    } else {
      throw new NotFoundException("No sell matches that buy order");
    }
  }

  @Override
  public SellPojo readOne(Predicate conditions) throws NotFoundException {
    Optional<Sell> matchingSell = salesRepository.findOne(conditions);
    if (matchingSell.isPresent()) {
      Sell found = matchingSell.get();
      SellPojo foundPojo = this.convertToPojo(found);
      this.applyDetails(found, foundPojo);
      return foundPojo;
    } else {
      throw new NotFoundException("No sell matches the filtering conditions");
    }
  }

  @Override
  public void setSellStatusToPaymentStartedWithToken(Long id, String token) throws NotFoundException {
    this.setSellStatusByName(id, "Payment Started");
    salesRepository.setTransactionToken(id, token);
  }

  @Override
  public void setSellStatusToPaymentAborted(Long id) throws NotFoundException {
    this.setSellStatusByName(id, "Payment Cancelled");
  }

  @Override
  public void setSellStatusToPaymentFailed(Long id) throws NotFoundException {
    this.setSellStatusByName(id, "Payment Failed");
  }

  @Override
  public void setSellStatusToPaidUnconfirmed(Long id) throws NotFoundException {
    this.setSellStatusByName(id, "Paid, Unconfirmed");
  }

  @Override
  public void setSellStatusToPaidConfirmed(Long id) throws NotFoundException {
    this.setSellStatusByName(id, "Paid, Confirmed");
  }

  @Override
  public void setSellStatusToRejected(Long id) throws NotFoundException {
    this.setSellStatusByName(id, "Rejected");
  }

  @Override
  public void setSellStatusToCompleted(Long id) throws NotFoundException {
    this.setSellStatusByName(id, "Delivery Complete");
  }

  @Transactional
  private void setSellStatusByName(Long sellId, String statusName) throws NotFoundException {
    if (!salesRepository.existsById(sellId)) {
      throw new NotFoundException("The specified sell does not exist");
    } else {
      Optional<SellStatus> statusEntityByName = statusesRepository.findByName(statusName);
      if (statusEntityByName.isPresent()) {
        SellStatus statusEntity = statusEntityByName.get();
        Integer statusChangeResponse = salesRepository.setStatus(sellId, statusEntity);
        logger.debug("statusChangeResponse={}", statusChangeResponse);
      } else {
        logger.error("No sell status exists by the name '{}'", statusName);
      }
    }
  }

  private void applyStatus(SellPojo source, Sell target) throws BadInputException {
    String statusName = source.getStatus();
    if (statusName == null || statusName.isBlank()) {
      statusName = "Pending";
    }

    Optional<SellStatus> existingStatus = statusesRepository.findByName(statusName);
    if (existingStatus.isEmpty()) {
      throw new BadInputException("Status '" + statusName + "' is not valid");
    } else {
      target.setStatus(existingStatus.get());
    }
  }

  private void applyPaymentType(SellPojo source, Sell target) throws BadInputException {
    String paymentType = source.getPaymentType();
    if (paymentType == null || paymentType.isBlank()) {
      throw new BadInputException("An accepted payment type is required");
    } else {
      Optional<PaymentType> existingPaymentType = paymentTypesRepository.findByName(paymentType);
      if (existingPaymentType.isEmpty()) {
        throw new BadInputException("Payment type '" + paymentType + "' is not valid");
      } else {
        target.setPaymentType(existingPaymentType.get());
      }
    }
  }

  private void applyBillingTypeAndCompany(SellPojo source, Sell target) throws BadInputException {
    String billingType = source.getBillingType();
    if (billingType == null || billingType.isBlank()) {
      billingType = "Bill";
    }

    Optional<BillingType> existingBillingType = billingTypesRepository.findByName(billingType);
    if (existingBillingType.isPresent()) {
      target.setBillingType(existingBillingType.get());
    } else {
      throw new BadInputException("Billing type '" + billingType + "' is not valid");
    }

    if (billingType.equals("Enterprise Invoice")) {
      BillingCompanyPojo sourceBillingCompany = source.getBillingCompany();
      if (sourceBillingCompany == null) {
        throw new BadInputException("Billing company details are required to generate enterprise invoices");
      } else {
        BillingCompany billingCompany = fetchOrConvertBillingCompany(target, sourceBillingCompany);
        target.setBillingCompany(billingCompany);
      }
    }
  }

  private void applyCustomer(SellPojo source, Sell target) throws BadInputException {
    CustomerPojo sourceCustomer = source.getCustomer();
    if (sourceCustomer == null) {
      throw new BadInputException("Customer must posess valid personal information");
    } else {
      Optional<Customer> existing = customersService.getExisting(sourceCustomer);
      Customer targetCustomer;
      if (existing.isPresent()) {
        targetCustomer = existing.get();
      } else {
        targetCustomer = customersService.convertToNewEntity(sourceCustomer);
        targetCustomer = customersRepository.saveAndFlush(targetCustomer);
      }
      target.setCustomer(targetCustomer);
    }
  }

  private void applyBillingAddress(SellPojo source, Sell target) throws BadInputException {
    AddressPojo billingAddress = source.getBillingAddress();
    if (billingAddress != null) {
      try {
        Address targetAddress = this.fetchOrConvertAddress(billingAddress);
        target.setBillingAddress(targetAddress);
      } catch (BadInputException ex) {
        throw new BadInputException("The provided billing address is not valid");
      }
    }
  }

  private void applyShippingAddress(SellPojo source, Sell target) throws BadInputException {
    AddressPojo shippingAddress = source.getShippingAddress();
    if (shippingAddress != null) {
      try {
        Address targetAddress = this.fetchOrConvertAddress(shippingAddress);
        target.setShippingAddress(targetAddress);
      } catch (BadInputException ex) {
        throw new BadInputException("The provided shipping address is not valid");
      }
    }
  }

  private void applyShipper(SellPojo source, Sell target) throws BadInputException {
    ShipperPojo sourceShipper = source.getShipper();
    if (sourceShipper != null) {
      Set<ConstraintViolation<ShipperPojo>> validations = validator.validate(sourceShipper);
      if (!validations.isEmpty()) {
        throw new BadInputException("Invalid shipper");
      } else {
        Optional<Shipper> byName = shippersRepository.findByName(sourceShipper.getName());
        if (byName.isEmpty()) {
          throw new BadInputException("The specified shipper does not exist");
        } else {
          target.setShipper(byName.get());
        }
      }
    }
  }

  private void applyDetails(SellPojo source, Sell target) throws BadInputException {
    Collection<SellDetailPojo> sourceDetails = source.getDetails();
    if (sourceDetails != null && !sourceDetails.isEmpty()) {
      List<SellDetail> details = new ArrayList<>();
      int netValue = 0, totalItems = 0;
      for (SellDetailPojo d : source.getDetails()) {
        SellDetail targetDetail = new SellDetail();
        String barcode = d.getProduct().getBarcode();
        Optional<Product> productByBarcode = productsRepository.findByBarcode(barcode);
        if (productByBarcode.isEmpty()) {
          throw new BadInputException("Unexisting product in sell details");
        } else {
          Product targetProduct = productByBarcode.get();
          targetDetail.setProduct(targetProduct);
          targetDetail.setUnits(d.getUnits());
        }
        details.add(targetDetail);
        int units = targetDetail.getUnits();
        netValue += (targetDetail.getProduct().getPrice() * units);
        totalItems += units;
      }
      target.setDetails(details);
      target.setNetValue(netValue);
      target.setTotalItems(totalItems);
      // TODO note where to add total value, transport value...?
    }
  }

  private Address fetchOrConvertAddress(AddressPojo source) throws BadInputException {
    Set<ConstraintViolation<AddressPojo>> validations = validator.validate(source);
    if (!validations.isEmpty()) {
      throw new BadInputException("Invalid address");
    } else {
      Optional<Address> matchingAddress = addressesRepository.findByFields(
        source.getCity(),
        source.getMunicipality(),
        source.getFirstLine(),
        source.getSecondLine(),
        source.getPostalCode(),
        source.getNotes());
      return matchingAddress.orElseGet(() -> conversion.convert(source, Address.class));
    }
  }

  private BillingCompany fetchOrConvertBillingCompany(Sell target, BillingCompanyPojo sourceBillingCompany)
    throws BadInputException {
    String idNumber = sourceBillingCompany.getIdNumber();
    if (idNumber == null || idNumber.isBlank() ) {
      throw new BadInputException("Billing company must have an id number");
    } else if (Pattern.compile("^\\d{7,9}[\\dk]$").matcher(idNumber).matches()) { // TODO parameterize regex
      throw new BadInputException("Billing company must have a correct id number");
    } else {
      Optional<BillingCompany> matchByIdNumber = billingCompaniesRepository.findByIdNumber(idNumber);
      if (matchByIdNumber.isPresent()) {
        return matchByIdNumber.get();
      } else {
        BillingCompany billingCompany = billingCompaniesService.convertToNewEntity(sourceBillingCompany);
        billingCompany = billingCompaniesRepository.saveAndFlush(billingCompany);
        return billingCompany;
      }
    }
  }

  private void applyDetails(Sell source, SellPojo target) {
    Collection<SellDetail> details = source.getDetails();
    if (details != null && !details.isEmpty()) {
      List<SellDetailPojo> sellDetails = new ArrayList<>();
      for (SellDetail sourceSellDetail : details) {
        ProductPojo product = conversion.convert(sourceSellDetail.getProduct(), ProductPojo.class);
        SellDetailPojo targetSellDetail = conversion.convert(sourceSellDetail, SellDetailPojo.class);
        if (product != null && targetSellDetail != null) {
          targetSellDetail.setProduct(product);
          sellDetails.add(targetSellDetail);
        }
      }
      target.setDetails(sellDetails);
    }
  }
}
