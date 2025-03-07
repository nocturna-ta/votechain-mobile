package com.devissvtr.wallet.contracts

import com.devissvtr.wallet.contracts.Votechain
import com.devissvtr.wallet.contracts.Votechain.KPUBranch
import com.devissvtr.wallet.contracts.Votechain.Voter
import io.reactivex.Flowable
import org.web3j.abi.EventEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.DynamicStruct
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.RemoteCall
import org.web3j.protocol.core.RemoteFunctionCall
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.BaseEventResponse
import org.web3j.protocol.core.methods.response.Log
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tuples.generated.Tuple4
import org.web3j.tuples.generated.Tuple5
import org.web3j.tx.Contract
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger
import java.util.Arrays

/**
 *
 * Auto generated code.
 *
 * **Do not modify!**
 *
 * Please use the [web3j command line tools](https://docs.web3j.io/command_line.html),
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * [codegen module](https://github.com/hyperledger-web3j/web3j/tree/main/codegen) to update.
 *
 *
 * Generated with web3j version 1.6.2.
 */
class Votechain : Contract {
    @Deprecated("")
    protected constructor(
        contractAddress: String?, web3j: Web3j?, credentials: Credentials,
        gasPrice: BigInteger?, gasLimit: BigInteger?
    ) : super(
        BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit
    )

    protected constructor(
        contractAddress: String?, web3j: Web3j?, credentials: Credentials,
        contractGasProvider: ContractGasProvider?
    ) : super(BINARY, contractAddress, web3j, credentials, contractGasProvider)

    @Deprecated("")
    protected constructor(
        contractAddress: String?, web3j: Web3j?, transactionManager: TransactionManager?,
        gasPrice: BigInteger?, gasLimit: BigInteger?
    ) : super(
        BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit
    )

    protected constructor(
        contractAddress: String?, web3j: Web3j?, transactionManager: TransactionManager?,
        contractGasProvider: ContractGasProvider?
    ) : super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider)

    fun candidateAddedEventFlowable(filter: EthFilter?): Flowable<CandidateAddedEventResponse> {
        return web3j.ethLogFlowable(filter).map { log: Log ->
            getCandidateAddedEventFromLog(
                log
            )
        }
    }

    fun candidateAddedEventFlowable(
        startBlock: DefaultBlockParameter?, endBlock: DefaultBlockParameter?
    ): Flowable<CandidateAddedEventResponse> {
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(CANDIDATEADDED_EVENT))
        return candidateAddedEventFlowable(filter)
    }

    fun candidateStatusChangedEventFlowable(
        filter: EthFilter?
    ): Flowable<CandidateStatusChangedEventResponse> {
        return web3j.ethLogFlowable(filter).map { log: Log ->
            getCandidateStatusChangedEventFromLog(
                log
            )
        }
    }

    fun candidateStatusChangedEventFlowable(
        startBlock: DefaultBlockParameter?, endBlock: DefaultBlockParameter?
    ): Flowable<CandidateStatusChangedEventResponse> {
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(CANDIDATESTATUSCHANGED_EVENT))
        return candidateStatusChangedEventFlowable(filter)
    }

    fun kPUBranchDeactivatedEventFlowable(
        filter: EthFilter?
    ): Flowable<KPUBranchDeactivatedEventResponse> {
        return web3j.ethLogFlowable(filter).map { log: Log ->
            getKPUBranchDeactivatedEventFromLog(
                log
            )
        }
    }

    fun kPUBranchDeactivatedEventFlowable(
        startBlock: DefaultBlockParameter?, endBlock: DefaultBlockParameter?
    ): Flowable<KPUBranchDeactivatedEventResponse> {
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(KPUBRANCHDEACTIVATED_EVENT))
        return kPUBranchDeactivatedEventFlowable(filter)
    }

    fun kPUBranchRegisteredEventFlowable(
        filter: EthFilter?
    ): Flowable<KPUBranchRegisteredEventResponse> {
        return web3j.ethLogFlowable(filter).map { log: Log ->
            getKPUBranchRegisteredEventFromLog(
                log
            )
        }
    }

    fun kPUBranchRegisteredEventFlowable(
        startBlock: DefaultBlockParameter?, endBlock: DefaultBlockParameter?
    ): Flowable<KPUBranchRegisteredEventResponse> {
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(KPUBRANCHREGISTERED_EVENT))
        return kPUBranchRegisteredEventFlowable(filter)
    }

    fun voteCastedEventFlowable(filter: EthFilter?): Flowable<VoteCastedEventResponse> {
        return web3j.ethLogFlowable(filter).map { log: Log ->
            getVoteCastedEventFromLog(
                log
            )
        }
    }

    fun voteCastedEventFlowable(
        startBlock: DefaultBlockParameter?, endBlock: DefaultBlockParameter?
    ): Flowable<VoteCastedEventResponse> {
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(VOTECASTED_EVENT))
        return voteCastedEventFlowable(filter)
    }

    fun voterRegisteredEventFlowable(filter: EthFilter?): Flowable<VoterRegisteredEventResponse> {
        return web3j.ethLogFlowable(filter).map { log: Log ->
            getVoterRegisteredEventFromLog(
                log
            )
        }
    }

    fun voterRegisteredEventFlowable(
        startBlock: DefaultBlockParameter?, endBlock: DefaultBlockParameter?
    ): Flowable<VoterRegisteredEventResponse> {
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(VOTERREGISTERED_EVENT))
        return voterRegisteredEventFlowable(filter)
    }

    fun votingStatusChangedEventFlowable(
        filter: EthFilter?
    ): Flowable<VotingStatusChangedEventResponse> {
        return web3j.ethLogFlowable(filter).map { log: Log ->
            getVotingStatusChangedEventFromLog(
                log
            )
        }
    }

    fun votingStatusChangedEventFlowable(
        startBlock: DefaultBlockParameter?, endBlock: DefaultBlockParameter?
    ): Flowable<VotingStatusChangedEventResponse> {
        val filter = EthFilter(startBlock, endBlock, getContractAddress())
        filter.addSingleTopic(EventEncoder.encode(VOTINGSTATUSCHANGED_EVENT))
        return votingStatusChangedEventFlowable(filter)
    }

    fun addCandidate(name: String?): RemoteFunctionCall<TransactionReceipt> {
        val function = Function(
            FUNC_ADDCANDIDATE,
            Arrays.asList<Type<*>>(Utf8String(name)),
            emptyList()
        )
        return executeRemoteCallTransaction(function)
    }

    fun candidateAddresses(
        param0: BigInteger?
    ): RemoteFunctionCall<Tuple4<String, String, BigInteger, Boolean>> {
        val function = Function(
            FUNC_CANDIDATEADDRESSES,
            Arrays.asList<Type<*>>(Uint256(param0)),
            Arrays.asList<TypeReference<*>>(
                object : TypeReference<Utf8String?>() {},
                object : TypeReference<Utf8String?>() {},
                object : TypeReference<Uint256?>() {},
                object : TypeReference<Bool?>() {})
        )
        return RemoteFunctionCall(
            function
        ) {
            val results = executeCallMultipleValueReturn(function)
            Tuple4(
                results[0].value as String,
                results[1].value as String,
                results[2].value as BigInteger,
                results[3].value as Boolean
            )
        }
    }

    fun candidateCount(): RemoteFunctionCall<BigInteger> {
        val function = Function(
            FUNC_CANDIDATECOUNT,
            mutableListOf(),
            Arrays.asList<TypeReference<*>>(object : TypeReference<Uint256?>() {})
        )
        return executeRemoteCallSingleValueReturn(function, BigInteger::class.java)
    }

    fun deactivateKPUBranch(branchAddress: String?): RemoteFunctionCall<TransactionReceipt> {
        val function = Function(
            FUNC_DEACTIVATEKPUBRANCH,
            Arrays.asList<Type<*>>(Address(160, branchAddress)),
            emptyList()
        )
        return executeRemoteCallTransaction(function)
    }

    val allCandidates: RemoteFunctionCall<List<*>>
        get() {
            val function = Function(
                FUNC_GETALLCANDIDATES,
                mutableListOf(),
                Arrays.asList<TypeReference<*>>(object :
                    TypeReference<DynamicArray<Candidate?>?>() {})
            )
            return RemoteFunctionCall(
                function
            ) {
                val result =
                    executeCallSingleValueReturn<Type<*>, List<*>>(
                        function,
                        MutableList::class.java
                    ) as List<Type<*>>
                convertToNative<Type<*>, Any>(
                    result
                )
            }
        }

    val allKPUBranches: RemoteFunctionCall<List<*>>
        get() {
            val function = Function(
                FUNC_GETALLKPUBRANCHES,
                mutableListOf(),
                Arrays.asList<TypeReference<*>>(object :
                    TypeReference<DynamicArray<KPUBranch?>?>() {})
            )
            return RemoteFunctionCall(
                function
            ) {
                val result =
                    executeCallSingleValueReturn<Type<*>, List<*>>(
                        function,
                        MutableList::class.java
                    ) as List<Type<*>>
                convertToNative<Type<*>, Any>(
                    result
                )
            }
        }

    val allVoter: RemoteFunctionCall<List<*>>
        get() {
            val function = Function(
                FUNC_GETALLVOTER,
                mutableListOf(),
                Arrays.asList<TypeReference<*>>(object : TypeReference<DynamicArray<Voter?>?>() {})
            )
            return RemoteFunctionCall(
                function
            ) {
                val result =
                    executeCallSingleValueReturn<Type<*>, List<*>>(
                        function,
                        MutableList::class.java
                    ) as List<Type<*>>
                convertToNative<Type<*>, Any>(
                    result
                )
            }
        }

    fun getBranchByAddress(branchAddress: String?): RemoteFunctionCall<KPUBranch> {
        val function = Function(
            FUNC_GETBRANCHBYADDRESS,
            Arrays.asList<Type<*>>(Address(160, branchAddress)),
            Arrays.asList<TypeReference<*>>(object : TypeReference<KPUBranch?>() {})
        )
        return executeRemoteCallSingleValueReturn(function, KPUBranch::class.java)
    }

    fun getCandidateByID(candidateId: String?): RemoteFunctionCall<Candidate> {
        val function = Function(
            FUNC_GETCANDIDATEBYID,
            Arrays.asList<Type<*>>(Utf8String(candidateId)),
            Arrays.asList<TypeReference<*>>(object : TypeReference<Candidate?>() {})
        )
        return executeRemoteCallSingleValueReturn(
            function,
            Candidate::class.java
        )
    }

    fun getVoterByAddress(voterAddress: String?): RemoteFunctionCall<Voter> {
        val function = Function(
            FUNC_GETVOTERBYADDRESS,
            Arrays.asList<Type<*>>(Address(160, voterAddress)),
            Arrays.asList<TypeReference<*>>(object : TypeReference<Voter?>() {})
        )
        return executeRemoteCallSingleValueReturn(function, Voter::class.java)
    }

    fun getVoterByNIK(nik: String?): RemoteFunctionCall<Voter> {
        val function = Function(
            FUNC_GETVOTERBYNIK,
            Arrays.asList<Type<*>>(Utf8String(nik)),
            Arrays.asList<TypeReference<*>>(object : TypeReference<Voter?>() {})
        )
        return executeRemoteCallSingleValueReturn(function, Voter::class.java)
    }

    fun getVoterByRegion(region: String?): RemoteFunctionCall<List<*>> {
        val function = Function(
            FUNC_GETVOTERBYREGION,
            Arrays.asList<Type<*>>(Utf8String(region)),
            Arrays.asList<TypeReference<*>>(object : TypeReference<DynamicArray<Voter?>?>() {})
        )
        return RemoteFunctionCall(
            function
        ) {
            val result =
                executeCallSingleValueReturn<Type<*>, List<*>>(
                    function,
                    MutableList::class.java
                ) as List<Type<*>>
            convertToNative<Type<*>, Any>(
                result
            )
        }
    }

    fun kpuAdmin(): RemoteFunctionCall<String> {
        val function = Function(
            FUNC_KPUADMIN,
            mutableListOf(),
            Arrays.asList<TypeReference<*>>(object : TypeReference<Address?>() {})
        )
        return executeRemoteCallSingleValueReturn(function, String::class.java)
    }

    fun kpuBranchAddresses(
        param0: BigInteger?
    ): RemoteFunctionCall<Tuple4<String, String, Boolean, String>> {
        val function = Function(
            FUNC_KPUBRANCHADDRESSES,
            Arrays.asList<Type<*>>(Uint256(param0)),
            Arrays.asList<TypeReference<*>>(
                object : TypeReference<Utf8String?>() {},
                object : TypeReference<Address?>() {},
                object : TypeReference<Bool?>() {},
                object : TypeReference<Utf8String?>() {})
        )
        return RemoteFunctionCall(
            function
        ) {
            val results = executeCallMultipleValueReturn(function)
            Tuple4(
                results[0].value as String,
                results[1].value as String,
                results[2].value as Boolean,
                results[3].value as String
            )
        }
    }

    fun kpuBranches(param0: String?): RemoteFunctionCall<Tuple4<String, String, Boolean, String>> {
        val function = Function(
            FUNC_KPUBRANCHES,
            Arrays.asList<Type<*>>(Address(160, param0)),
            Arrays.asList<TypeReference<*>>(
                object : TypeReference<Utf8String?>() {},
                object : TypeReference<Address?>() {},
                object : TypeReference<Bool?>() {},
                object : TypeReference<Utf8String?>() {})
        )
        return RemoteFunctionCall(
            function
        ) {
            val results = executeCallMultipleValueReturn(function)
            Tuple4(
                results[0].value as String,
                results[1].value as String,
                results[2].value as Boolean,
                results[3].value as String
            )
        }
    }

    fun registerKPUBranch(
        branchAddress: String?,
        name: String?, region: String?
    ): RemoteFunctionCall<TransactionReceipt> {
        val function = Function(
            FUNC_REGISTERKPUBRANCH,
            Arrays.asList<Type<*>>(
                Address(160, branchAddress),
                Utf8String(name),
                Utf8String(region)
            ),
            emptyList()
        )
        return executeRemoteCallTransaction(function)
    }

    fun registerVoter(nik: String?, voterAddress: String?): RemoteFunctionCall<TransactionReceipt> {
        val function = Function(
            FUNC_REGISTERVOTER,
            Arrays.asList<Type<*>>(
                Utf8String(nik),
                Address(160, voterAddress)
            ),
            emptyList()
        )
        return executeRemoteCallTransaction(function)
    }

    fun setKpuAdmin(newAdmin: String?): RemoteFunctionCall<TransactionReceipt> {
        val function = Function(
            FUNC_SETKPUADMIN,
            Arrays.asList<Type<*>>(Address(160, newAdmin)),
            emptyList()
        )
        return executeRemoteCallTransaction(function)
    }

    fun setVotingStatus(status: Boolean): RemoteFunctionCall<TransactionReceipt> {
        val function = Function(
            FUNC_SETVOTINGSTATUS,
            Arrays.asList<Type<*>>(Bool(status)),
            emptyList()
        )
        return executeRemoteCallTransaction(function)
    }

    fun toggleCandidateActive(candidateId: String?): RemoteFunctionCall<TransactionReceipt> {
        val function = Function(
            FUNC_TOGGLECANDIDATEACTIVE,
            Arrays.asList<Type<*>>(Utf8String(candidateId)),
            emptyList()
        )
        return executeRemoteCallTransaction(function)
    }

    fun vote(candidateId: String?): RemoteFunctionCall<TransactionReceipt> {
        val function = Function(
            FUNC_VOTE,
            Arrays.asList<Type<*>>(Utf8String(candidateId)),
            emptyList()
        )
        return executeRemoteCallTransaction(function)
    }

    fun voterAddresses(
        param0: BigInteger?
    ): RemoteFunctionCall<Tuple5<String, String, Boolean, String, Boolean>> {
        val function = Function(
            FUNC_VOTERADDRESSES,
            Arrays.asList<Type<*>>(Uint256(param0)),
            Arrays.asList<TypeReference<*>>(
                object : TypeReference<Utf8String?>() {},
                object : TypeReference<Address?>() {},
                object : TypeReference<Bool?>() {},
                object : TypeReference<Utf8String?>() {},
                object : TypeReference<Bool?>() {})
        )
        return RemoteFunctionCall(
            function
        ) {
            val results = executeCallMultipleValueReturn(function)
            Tuple5(
                results[0].value as String,
                results[1].value as String,
                results[2].value as Boolean,
                results[3].value as String,
                results[4].value as Boolean
            )
        }
    }

    fun voterNIKByAddress(param0: String?): RemoteFunctionCall<String> {
        val function = Function(
            FUNC_VOTERNIKBYADDRESS,
            Arrays.asList<Type<*>>(Address(160, param0)),
            Arrays.asList<TypeReference<*>>(object : TypeReference<Utf8String?>() {})
        )
        return executeRemoteCallSingleValueReturn(function, String::class.java)
    }

    fun voters(
        param0: String?
    ): RemoteFunctionCall<Tuple5<String, String, Boolean, String, Boolean>> {
        val function = Function(
            FUNC_VOTERS,
            Arrays.asList<Type<*>>(Utf8String(param0)),
            Arrays.asList<TypeReference<*>>(
                object : TypeReference<Utf8String?>() {},
                object : TypeReference<Address?>() {},
                object : TypeReference<Bool?>() {},
                object : TypeReference<Utf8String?>() {},
                object : TypeReference<Bool?>() {})
        )
        return RemoteFunctionCall(
            function
        ) {
            val results = executeCallMultipleValueReturn(function)
            Tuple5(
                results[0].value as String,
                results[1].value as String,
                results[2].value as Boolean,
                results[3].value as String,
                results[4].value as Boolean
            )
        }
    }

    fun votingActive(): RemoteFunctionCall<Boolean> {
        val function = Function(
            FUNC_VOTINGACTIVE,
            mutableListOf(),
            Arrays.asList<TypeReference<*>>(object : TypeReference<Bool?>() {})
        )
        return executeRemoteCallSingleValueReturn(function, Boolean::class.java)
    }

    class Candidate : DynamicStruct {
        var id: String

        var name: String

        var voteCount: BigInteger

        var isActive: Boolean

        constructor(id: String, name: String, voteCount: BigInteger, isActive: Boolean) : super(
            Utf8String(id),
            Utf8String(name),
            Uint256(voteCount),
            Bool(isActive)
        ) {
            this.id = id
            this.name = name
            this.voteCount = voteCount
            this.isActive = isActive
        }

        constructor(
            id: Utf8String,
            name: Utf8String,
            voteCount: Uint256,
            isActive: Bool
        ) : super(id, name, voteCount, isActive) {
            this.id = id.value
            this.name = name.value
            this.voteCount = voteCount.value
            this.isActive = isActive.value
        }
    }

    class KPUBranch : DynamicStruct {
        var name: String

        var branchAddress: String

        var isActive: Boolean

        var region: String

        constructor(name: String, branchAddress: String, isActive: Boolean, region: String) : super(
            Utf8String(name),
            Address(160, branchAddress),
            Bool(isActive),
            Utf8String(region)
        ) {
            this.name = name
            this.branchAddress = branchAddress
            this.isActive = isActive
            this.region = region
        }

        constructor(
            name: Utf8String,
            branchAddress: Address,
            isActive: Bool,
            region: Utf8String
        ) : super(name, branchAddress, isActive, region) {
            this.name = name.value
            this.branchAddress = branchAddress.value
            this.isActive = isActive.value
            this.region = region.value
        }
    }

    class Voter : DynamicStruct {
        var nik: String

        var voterAddress: String

        var hasVoted: Boolean

        var region: String

        var isRegistered: Boolean

        constructor(
            nik: String, voterAddress: String, hasVoted: Boolean, region: String,
            isRegistered: Boolean
        ) : super(
            Utf8String(nik),
            Address(160, voterAddress),
            Bool(hasVoted),
            Utf8String(region),
            Bool(isRegistered)
        ) {
            this.nik = nik
            this.voterAddress = voterAddress
            this.hasVoted = hasVoted
            this.region = region
            this.isRegistered = isRegistered
        }

        constructor(
            nik: Utf8String, voterAddress: Address, hasVoted: Bool, region: Utf8String,
            isRegistered: Bool
        ) : super(nik, voterAddress, hasVoted, region, isRegistered) {
            this.nik = nik.value
            this.voterAddress = voterAddress.value
            this.hasVoted = hasVoted.value
            this.region = region.value
            this.isRegistered = isRegistered.value
        }
    }

    class CandidateAddedEventResponse : BaseEventResponse() {
        var candidateId: ByteArray

        var name: String? = null
    }

    class CandidateStatusChangedEventResponse : BaseEventResponse() {
        var candidateId: BigInteger? = null

        var isActive: Boolean? = null
    }

    class KPUBranchDeactivatedEventResponse : BaseEventResponse() {
        var branchAddress: String? = null
    }

    class KPUBranchRegisteredEventResponse : BaseEventResponse() {
        var branchAddress: String? = null

        var name: String? = null

        var region: String? = null
    }

    class VoteCastedEventResponse : BaseEventResponse() {
        var nik: ByteArray

        var candidateId: ByteArray
    }

    class VoterRegisteredEventResponse : BaseEventResponse() {
        var nik: ByteArray

        var voterAddress: String? = null

        var region: String? = null
    }

    class VotingStatusChangedEventResponse : BaseEventResponse() {
        var isActive: Boolean? = null
    }

    companion object {
        const val BINARY: String =
            "6080604052348015600e575f5ffd5b505f80546001600160a01b0319163317905561313c8061002d5f395ff3fe608060405234801561000f575f5ffd5b506004361061016d575f3560e01c806392371451116100d9578063dd0e237311610093578063f0416e5f1161006e578063f0416e5f14610388578063f44f4e14146103a8578063fb4ab164146103b0578063fc36e15b146103da575f5ffd5b8063dd0e237314610335578063e0d5343b14610348578063efe3b01b14610368575f5ffd5b806392371451146102a05780639df86dc1146102c0578063a9a981a3146102d3578063ab165a3e146102ea578063b0c9b9a0146102fd578063d55d73c514610312575f5ffd5b8063462e91ec1161012a578063462e91ec1461021d5780634a075de2146102305780634bdd7585146102435780634c3614351461025657806353fa2e64146102695780637478c9fe1461028d575f5ffd5b8063027d85141461017157806304abffb5146101865780631999a15f146101b25780632a70ecca146101c55780632e6997fe146101e5578063408e2727146101fa575b5f5ffd5b61018461017f3660046127c3565b6103ed565b005b610199610194366004612843565b610766565b6040516101a99493929190612888565b60405180910390f35b6101846101c03660046128ce565b6108bf565b6101d86101d33660046128ce565b6109e2565b6040516101a99190612971565b6101ed610b98565b6040516101a991906129da565b5f5461020d90600160a01b900460ff1681565b60405190151581526020016101a9565b61018461022b3660046128ce565b610d32565b61018461023e366004612a3d565b610e95565b6101d8610251366004612a8d565b611234565b610184610264366004612a8d565b6114a7565b61027c610277366004612aba565b611561565b6040516101a9959493929190612b6d565b61018461029b366004612bbd565b6116bb565b6102b36102ae3660046128ce565b61173c565b6040516101a99190612bdc565b6101846102ce366004612a8d565b611954565b6102dc60015481565b6040519081526020016101a9565b6101996102f8366004612a8d565b61199f565b6103056119b9565b6040516101a99190612c42565b610325610320366004612843565b611b54565b6040516101a99493929190612c99565b61027c610343366004612843565b611c9f565b61035b6103563660046128ce565b611ccd565b6040516101a99190612cd6565b61037b610376366004612a8d565b611fe3565b6040516101a99190612d2d565b61039b610396366004612a8d565b6121ca565b6040516101a99190612d3f565b61035b612261565b5f546103c2906001600160a01b031681565b6040516001600160a01b0390911681526020016101a9565b6101846103e83660046128ce565b61240e565b5f546001600160a01b03163314610417576040516322e2563760e21b815260040160405180910390fd5b6001600160a01b0385165f90815260026020526040902060010154600160a01b900460ff161561045a576040516369f76f4360e11b815260040160405180910390fd5b604051806080016040528085858080601f0160208091040260200160405190810160405280939291908181526020018383808284375f920191909152505050908252506001600160a01b03871660208083019190915260016040808401919091528051601f86018390048302810183019091528481526060909201919085908590819084018382808284375f9201829052509390945250506001600160a01b0388168152600260205260409020825190915081906105189082612dd4565b50602082015160018201805460408501511515600160a01b026001600160a81b03199091166001600160a01b0390931692909217919091179055606082015160028201906105669082612dd4565b509050505f604051806080016040528086868080601f0160208091040260200160405190810160405280939291908181526020018383808284375f920191909152505050908252506001600160a01b03881660208083019190915260016040808401919091528051601f87018390048302810183019091528581526060909201919086908690819084018382808284375f9201829052509390945250506001600160a01b038916815260026020526040902082519293508392909150819061062e9082612dd4565b50602082015160018201805460408501511515600160a01b026001600160a81b03199091166001600160a01b03909316929092179190911790556060820151600282019061067c9082612dd4565b5050600580546001810182555f91909152825183925060039091027f036b6384b5eca791c62761152d0c79bb0604c104a5fb6f4eb0703f3154bb3db0019081906106c69082612dd4565b50602082015160018201805460408501511515600160a01b026001600160a81b03199091166001600160a01b0390931692909217919091179055606082015160028201906107149082612dd4565b505050856001600160a01b03167fc4e2c95246cc050bddc27763a59824d38df0df18e23f19099623d7e1618790f6868686866040516107569493929190612eb7565b60405180910390a2505050505050565b60058181548110610775575f80fd5b905f5260205f2090600302015f91509050805f01805461079490612d51565b80601f01602080910402602001604051908101604052809291908181526020018280546107c090612d51565b801561080b5780601f106107e25761010080835404028352916020019161080b565b820191905f5260205f20905b8154815290600101906020018083116107ee57829003601f168201915b50505050600183015460028401805493946001600160a01b03831694600160a01b90930460ff1693509161083e90612d51565b80601f016020809104026020016040519081016040528092919081815260200182805461086a90612d51565b80156108b55780601f1061088c576101008083540402835291602001916108b5565b820191905f5260205f20905b81548152906001019060200180831161089857829003601f168201915b5050505050905084565b5f546001600160a01b031633146108e9576040516322e2563760e21b815260040160405180910390fd5b5f805b6007548110156109be578383604051610906929190612edd565b60405180910390206007828154811061092157610921612eec565b905f5260205f2090600402015f0160405161093c9190612f00565b6040518091039020036109b6576007818154811061095c5761095c612eec565b905f5260205f2090600402016003015f9054906101000a900460ff16156007828154811061098c5761098c612eec565b5f9182526020909120600490910201600301805460ff1916911515919091179055600191506109be565b6001016108ec565b50806109dd5760405163e66ea08f60e01b815260040160405180910390fd5b505050565b6040805160a08101825260608082525f6020830181905292820183905280820152608081019190915260038383604051610a1d929190612edd565b90815260200160405180910390206040518060a00160405290815f82018054610a4590612d51565b80601f0160208091040260200160405190810160405280929190818152602001828054610a7190612d51565b8015610abc5780601f10610a9357610100808354040283529160200191610abc565b820191905f5260205f20905b815481529060010190602001808311610a9f57829003601f168201915b505050918352505060018201546001600160a01b0381166020830152600160a01b900460ff1615156040820152600282018054606090920191610afe90612d51565b80601f0160208091040260200160405190810160405280929190818152602001828054610b2a90612d51565b8015610b755780601f10610b4c57610100808354040283529160200191610b75565b820191905f5260205f20905b815481529060010190602001808311610b5857829003601f168201915b50505091835250506003919091015460ff16151560209091015290505b92915050565b60606007805480602002602001604051908101604052809291908181526020015f905b82821015610d29578382905f5260205f2090600402016040518060800160405290815f82018054610beb90612d51565b80601f0160208091040260200160405190810160405280929190818152602001828054610c1790612d51565b8015610c625780601f10610c3957610100808354040283529160200191610c62565b820191905f5260205f20905b815481529060010190602001808311610c4557829003601f168201915b50505050508152602001600182018054610c7b90612d51565b80601f0160208091040260200160405190810160405280929190818152602001828054610ca790612d51565b8015610cf25780601f10610cc957610100808354040283529160200191610cf2565b820191905f5260205f20905b815481529060010190602001808311610cd557829003601f168201915b5050509183525050600282015460208083019190915260039092015460ff1615156040909101529082526001929092019101610bbb565b50505050905090565b5f546001600160a01b03163314610d5c576040516322e2563760e21b815260040160405180910390fd5b5f610d65612715565b90505f604051806080016040528083815260200185858080601f0160208091040260200160405190810160405280939291908181526020018383808284375f920182905250938552505050602082018190526001604090920182905260078054928301815590528151919250829160049091027fa66cc928b5edb82af9bd49922954155ab7b0942694bea4ce44661d9a8736c68801908190610e079082612dd4565b5060208201516001820190610e1c9082612dd4565b5060408281015160028301556060909201516003909101805460ff191691151591909117905551610e4e908390612f71565b60405180910390207f6184463fde61a3e60869da6c5a223163f4b7548f805e17f865da2e8d3828d3378585604051610e87929190612f87565b60405180910390a250505050565b335f90815260026020526040902060010154600160a01b900460ff16610ece5760405163396382bd60e11b815260040160405180910390fd5b60038383604051610ee0929190612edd565b9081526040519081900360200190206003015460ff1615610f14576040516359a1ec4760e01b815260040160405180910390fd5b6001600160a01b0381165f9081526004602052604090208054610f3690612d51565b159050610f56576040516316a163b960e11b815260040160405180910390fd5b6040805160c06020601f8601819004028201810190925260a081018481525f9282919087908790819085018382808284375f9201829052509385525050506001600160a01b0385166020808401919091526040808401839052338352600291829052909120018054606090920191610fcd90612d51565b80601f0160208091040260200160405190810160405280929190818152602001828054610ff990612d51565b80156110445780601f1061101b57610100808354040283529160200191611044565b820191905f5260205f20905b81548152906001019060200180831161102757829003601f168201915b50505050508152602001600115158152509050806003858560405161106a929190612edd565b908152604051908190036020019020815181906110879082612dd4565b50602082015160018201805460408501511515600160a01b026001600160a81b03199091166001600160a01b0390931692909217919091179055606082015160028201906110d59082612dd4565b50608091909101516003909101805460ff19169115159190911790556001600160a01b0382165f908152600460205260409020611113848683612fa2565b50600680546001810182555f91909152815182916004027ff652222313e28459528d920b65115c16c04f3efc82aaedc97be59f3f377c0d3f019081906111599082612dd4565b50602082015160018201805460408501511515600160a01b026001600160a81b03199091166001600160a01b0390931692909217919091179055606082015160028201906111a79082612dd4565b50608091909101516003909101805460ff19169115159190911790556040516001600160a01b038316906111de9086908690612edd565b60408051918290038220335f9081526002602081905292902090927fe8bf381bec3899d7c4d98d7e52cfd45dfe7254b2ceafbb4d6dca1235ed10624d9261122692019061305c565b60405180910390a350505050565b6040805160a08101825260608082525f602083018190529282018390528082015260808101919091526001600160a01b0382165f908152600460205260408120805461127f90612d51565b80601f01602080910402602001604051908101604052809291908181526020018280546112ab90612d51565b80156112f65780601f106112cd576101008083540402835291602001916112f6565b820191905f5260205f20905b8154815290600101906020018083116112d957829003601f168201915b5050505050905080515f0361131e57604051636f08c58760e01b815260040160405180910390fd5b60038160405161132e9190612f71565b90815260200160405180910390206040518060a00160405290815f8201805461135690612d51565b80601f016020809104026020016040519081016040528092919081815260200182805461138290612d51565b80156113cd5780601f106113a4576101008083540402835291602001916113cd565b820191905f5260205f20905b8154815290600101906020018083116113b057829003601f168201915b505050918352505060018201546001600160a01b0381166020830152600160a01b900460ff161515604082015260028201805460609092019161140f90612d51565b80601f016020809104026020016040519081016040528092919081815260200182805461143b90612d51565b80156114865780601f1061145d57610100808354040283529160200191611486565b820191905f5260205f20905b81548152906001019060200180831161146957829003601f168201915b50505091835250506003919091015460ff1615156020909101529392505050565b5f546001600160a01b031633146114d1576040516322e2563760e21b815260040160405180910390fd5b6001600160a01b0381165f90815260026020526040902060010154600160a01b900460ff166115135760405163c75d5c6560e01b815260040160405180910390fd5b6001600160a01b0381165f81815260026020526040808220600101805460ff60a01b19169055517f3954ed3404fab00c2419751b1587a1f6655215f0dc812ab5ed515d4c2677b2d39190a250565b805160208183018101805160038252928201919093012091528054819061158790612d51565b80601f01602080910402602001604051908101604052809291908181526020018280546115b390612d51565b80156115fe5780601f106115d5576101008083540402835291602001916115fe565b820191905f5260205f20905b8154815290600101906020018083116115e157829003601f168201915b50505050600183015460028401805493946001600160a01b03831694600160a01b90930460ff1693509161163190612d51565b80601f016020809104026020016040519081016040528092919081815260200182805461165d90612d51565b80156116a85780601f1061167f576101008083540402835291602001916116a8565b820191905f5260205f20905b81548152906001019060200180831161168b57829003601f168201915b5050506003909301549192505060ff1685565b5f546001600160a01b031633146116e5576040516322e2563760e21b815260040160405180910390fd5b5f8054821515600160a01b0260ff60a01b199091161790556040517f9069a1a16ace751e8690f383e12f87b01e8488ba387e626810bd113fef0417f99061173190831515815260200190565b60405180910390a150565b611767604051806080016040528060608152602001606081526020015f81526020015f151581525090565b5f5b60075481101561193a578383604051611783929190612edd565b60405180910390206007828154811061179e5761179e612eec565b905f5260205f2090600402015f016040516117b99190612f00565b60405180910390200361193257600781815481106117d9576117d9612eec565b905f5260205f2090600402016040518060800160405290815f820180546117ff90612d51565b80601f016020809104026020016040519081016040528092919081815260200182805461182b90612d51565b80156118765780601f1061184d57610100808354040283529160200191611876565b820191905f5260205f20905b81548152906001019060200180831161185957829003601f168201915b5050505050815260200160018201805461188f90612d51565b80601f01602080910402602001604051908101604052809291908181526020018280546118bb90612d51565b80156119065780601f106118dd57610100808354040283529160200191611906565b820191905f5260205f20905b8154815290600101906020018083116118e957829003601f168201915b50505091835250506002820154602082015260039091015460ff1615156040909101529150610b929050565b600101611769565b5060405163e66ea08f60e01b815260040160405180910390fd5b5f546001600160a01b0316331461197e576040516322e2563760e21b815260040160405180910390fd5b5f80546001600160a01b0319166001600160a01b0392909216919091179055565b60026020525f908152604090208054819061079490612d51565b60606005805480602002602001604051908101604052809291908181526020015f905b82821015610d29578382905f5260205f2090600302016040518060800160405290815f82018054611a0c90612d51565b80601f0160208091040260200160405190810160405280929190818152602001828054611a3890612d51565b8015611a835780601f10611a5a57610100808354040283529160200191611a83565b820191905f5260205f20905b815481529060010190602001808311611a6657829003601f168201915b505050918352505060018201546001600160a01b0381166020830152600160a01b900460ff1615156040820152600282018054606090920191611ac590612d51565b80601f0160208091040260200160405190810160405280929190818152602001828054611af190612d51565b8015611b3c5780601f10611b1357610100808354040283529160200191611b3c565b820191905f5260205f20905b815481529060010190602001808311611b1f57829003601f168201915b505050505081525050815260200190600101906119dc565b60078181548110611b63575f80fd5b905f5260205f2090600402015f91509050805f018054611b8290612d51565b80601f0160208091040260200160405190810160405280929190818152602001828054611bae90612d51565b8015611bf95780601f10611bd057610100808354040283529160200191611bf9565b820191905f5260205f20905b815481529060010190602001808311611bdc57829003601f168201915b505050505090806001018054611c0e90612d51565b80601f0160208091040260200160405190810160405280929190818152602001828054611c3a90612d51565b8015611c855780601f10611c5c57610100808354040283529160200191611c85565b820191905f5260205f20905b815481529060010190602001808311611c6857829003601f168201915b50505050600283015460039093015491929160ff16905084565b60068181548110611cae575f80fd5b905f5260205f2090600402015f91509050805f01805461158790612d51565b60605f805b600654811015611d46578484604051611cec929190612edd565b604051809103902060068281548110611d0757611d07612eec565b905f5260205f209060040201600201604051611d239190612f00565b604051809103902003611d3e5781611d3a816130e2565b9250505b600101611cd2565b505f8167ffffffffffffffff811115611d6157611d61612aa6565b604051908082528060200260200182016040528015611dbb57816020015b6040805160a08101825260608082525f60208301819052928201839052808201526080810191909152815260200190600190039081611d7f5790505b5090505f805b600654811015611fd8578686604051611ddb929190612edd565b604051809103902060068281548110611df657611df6612eec565b905f5260205f209060040201600201604051611e129190612f00565b604051809103902003611fd05760068181548110611e3257611e32612eec565b905f5260205f2090600402016040518060a00160405290815f82018054611e5890612d51565b80601f0160208091040260200160405190810160405280929190818152602001828054611e8490612d51565b8015611ecf5780601f10611ea657610100808354040283529160200191611ecf565b820191905f5260205f20905b815481529060010190602001808311611eb257829003601f168201915b505050918352505060018201546001600160a01b0381166020830152600160a01b900460ff1615156040820152600282018054606090920191611f1190612d51565b80601f0160208091040260200160405190810160405280929190818152602001828054611f3d90612d51565b8015611f885780601f10611f5f57610100808354040283529160200191611f88565b820191905f5260205f20905b815481529060010190602001808311611f6b57829003601f168201915b50505091835250506003919091015460ff1615156020909101528351849084908110611fb657611fb6612eec565b60200260200101819052508180611fcc906130e2565b9250505b600101611dc1565b509095945050505050565b6120176040518060800160405280606081526020015f6001600160a01b031681526020015f15158152602001606081525090565b6001600160a01b0382165f90815260026020526040902060010154600160a01b900460ff166120595760405163c75d5c6560e01b815260040160405180910390fd5b6001600160a01b0382165f908152600260205260409081902081516080810190925280548290829061208a90612d51565b80601f01602080910402602001604051908101604052809291908181526020018280546120b690612d51565b80156121015780601f106120d857610100808354040283529160200191612101565b820191905f5260205f20905b8154815290600101906020018083116120e457829003601f168201915b505050918352505060018201546001600160a01b0381166020830152600160a01b900460ff161515604082015260028201805460609092019161214390612d51565b80601f016020809104026020016040519081016040528092919081815260200182805461216f90612d51565b80156121ba5780601f10612191576101008083540402835291602001916121ba565b820191905f5260205f20905b81548152906001019060200180831161219d57829003601f168201915b5050505050815250509050919050565b60046020525f9081526040902080546121e290612d51565b80601f016020809104026020016040519081016040528092919081815260200182805461220e90612d51565b80156122595780601f1061223057610100808354040283529160200191612259565b820191905f5260205f20905b81548152906001019060200180831161223c57829003601f168201915b505050505081565b60606006805480602002602001604051908101604052809291908181526020015f905b82821015610d29578382905f5260205f2090600402016040518060a00160405290815f820180546122b490612d51565b80601f01602080910402602001604051908101604052809291908181526020018280546122e090612d51565b801561232b5780601f106123025761010080835404028352916020019161232b565b820191905f5260205f20905b81548152906001019060200180831161230e57829003601f168201915b505050918352505060018201546001600160a01b0381166020830152600160a01b900460ff161515604082015260028201805460609092019161236d90612d51565b80601f016020809104026020016040519081016040528092919081815260200182805461239990612d51565b80156123e45780601f106123bb576101008083540402835291602001916123e4565b820191905f5260205f20905b8154815290600101906020018083116123c757829003601f168201915b50505091835250506003919091015460ff1615156020918201529082526001929092019101612284565b5f54600160a01b900460ff1661243757604051639b8cc47560e01b815260040160405180910390fd5b335f908152600460205260408120805461245090612d51565b80601f016020809104026020016040519081016040528092919081815260200182805461247c90612d51565b80156124c75780601f1061249e576101008083540402835291602001916124c7565b820191905f5260205f20905b8154815290600101906020018083116124aa57829003601f168201915b5050505050905080515f036124ef57604051636f08c58760e01b815260040160405180910390fd5b5f6003826040516125009190612f71565b908152604051908190036020019020600181015490915060ff600160a01b909104161561254057604051637c9a1cf960e01b815260040160405180910390fd5b5f805b6007548110156125e357858560405161255d929190612edd565b60405180910390206007828154811061257857612578612eec565b905f5260205f2090600402015f016040516125939190612f00565b60405180910390201480156125cd5750600781815481106125b6576125b6612eec565b5f91825260209091206003600490920201015460ff165b156125db57600191506125e3565b600101612543565b50806126025760405163e66ea08f60e01b815260040160405180910390fd5b60018201805460ff60a01b1916600160a01b1790555f5b6007548110156126b8578585604051612633929190612edd565b60405180910390206007828154811061264e5761264e612eec565b905f5260205f2090600402015f016040516126699190612f00565b6040518091039020036126b0576007818154811061268957612689612eec565b5f918252602082206002600490920201018054916126a6836130e2565b91905055506126b8565b600101612619565b5084846040516126c9929190612edd565b6040518091039020836040516126df9190612f71565b604051908190038120907f791f7d5f0b0d6e798f239ccca607156ff12f293d6709379301dc1a27e5206181905f90a35050505050565b6060423360405160200161274f929190918252602d60f81b602083015260601b6bffffffffffffffffffffffff1916602182015260350190565b604051602081830303815290604052905090565b80356001600160a01b0381168114612779575f5ffd5b919050565b5f5f83601f84011261278e575f5ffd5b50813567ffffffffffffffff8111156127a5575f5ffd5b6020830191508360208285010111156127bc575f5ffd5b9250929050565b5f5f5f5f5f606086880312156127d7575f5ffd5b6127e086612763565b9450602086013567ffffffffffffffff8111156127fb575f5ffd5b6128078882890161277e565b909550935050604086013567ffffffffffffffff811115612826575f5ffd5b6128328882890161277e565b969995985093965092949392505050565b5f60208284031215612853575f5ffd5b5035919050565b5f81518084528060208401602086015e5f602082860101526020601f19601f83011685010191505092915050565b608081525f61289a608083018761285a565b6001600160a01b0386166020840152841515604084015282810360608401526128c3818561285a565b979650505050505050565b5f5f602083850312156128df575f5ffd5b823567ffffffffffffffff8111156128f5575f5ffd5b6129018582860161277e565b90969095509350505050565b5f815160a0845261292160a085018261285a565b905060018060a01b03602084015116602085015260408301511515604085015260608301518482036060860152612958828261285a565b9150506080830151151560808501528091505092915050565b602081525f612983602083018461290d565b9392505050565b5f81516080845261299e608085018261285a565b9050602083015184820360208601526129b7828261285a565b915050604083015160408501526060830151151560608501528091505092915050565b5f602082016020835280845180835260408501915060408160051b8601019250602086015f5b82811015612a3157603f19878603018452612a1c85835161298a565b94506020938401939190910190600101612a00565b50929695505050505050565b5f5f5f60408486031215612a4f575f5ffd5b833567ffffffffffffffff811115612a65575f5ffd5b612a718682870161277e565b9094509250612a84905060208501612763565b90509250925092565b5f60208284031215612a9d575f5ffd5b61298382612763565b634e487b7160e01b5f52604160045260245ffd5b5f60208284031215612aca575f5ffd5b813567ffffffffffffffff811115612ae0575f5ffd5b8201601f81018413612af0575f5ffd5b803567ffffffffffffffff811115612b0a57612b0a612aa6565b604051601f8201601f19908116603f0116810167ffffffffffffffff81118282101715612b3957612b39612aa6565b604052818152828201602001861015612b50575f5ffd5b816020840160208301375f91810160200191909152949350505050565b60a081525f612b7f60a083018861285a565b6001600160a01b038716602084015285151560408401528281036060840152612ba8818661285a565b91505082151560808301529695505050505050565b5f60208284031215612bcd575f5ffd5b81358015158114612983575f5ffd5b602081525f612983602083018461298a565b5f815160808452612c02608085018261285a565b905060018060a01b03602084015116602085015260408301511515604085015260608301518482036060860152612c39828261285a565b95945050505050565b5f602082016020835280845180835260408501915060408160051b8601019250602086015f5b82811015612a3157603f19878603018452612c84858351612bee565b94506020938401939190910190600101612c68565b608081525f612cab608083018761285a565b8281036020840152612cbd818761285a565b6040840195909552505090151560609091015292915050565b5f602082016020835280845180835260408501915060408160051b8601019250602086015f5b82811015612a3157603f19878603018452612d1885835161290d565b94506020938401939190910190600101612cfc565b602081525f6129836020830184612bee565b602081525f612983602083018461285a565b600181811c90821680612d6557607f821691505b602082108103612d8357634e487b7160e01b5f52602260045260245ffd5b50919050565b601f8211156109dd57805f5260205f20601f840160051c81016020851015612dae5750805b601f840160051c820191505b81811015612dcd575f8155600101612dba565b5050505050565b815167ffffffffffffffff811115612dee57612dee612aa6565b612e0281612dfc8454612d51565b84612d89565b6020601f821160018114612e34575f8315612e1d5750848201515b5f19600385901b1c1916600184901b178455612dcd565b5f84815260208120601f198516915b82811015612e635787850151825560209485019460019092019101612e43565b5084821015612e8057868401515f19600387901b60f8161c191681555b50505050600190811b01905550565b81835281816020850137505f828201602090810191909152601f909101601f19169091010190565b604081525f612eca604083018688612e8f565b82810360208401526128c3818587612e8f565b818382375f9101908152919050565b634e487b7160e01b5f52603260045260245ffd5b5f5f8354612f0d81612d51565b600182168015612f245760018114612f3957612f66565b60ff1983168652811515820286019350612f66565b865f5260205f205f5b83811015612f5e57815488820152600190910190602001612f42565b505081860193505b509195945050505050565b5f82518060208501845e5f920191825250919050565b602081525f612f9a602083018486612e8f565b949350505050565b67ffffffffffffffff831115612fba57612fba612aa6565b612fce83612fc88354612d51565b83612d89565b5f601f841160018114612fff575f8515612fe85750838201355b5f19600387901b1c1916600186901b178355612dcd565b5f83815260208120601f198716915b8281101561302e578685013582556020948501946001909201910161300e565b508682101561304a575f1960f88860031b161c19848701351681555b505060018560011b0183555050505050565b602081525f5f835461306d81612d51565b806020860152600182165f811461308b57600181146130a757612f66565b60ff1983166040870152604082151560051b8701019350612f66565b865f5260205f205f5b838110156130cf578154888201604001526001909101906020016130b0565b8701604001945050509195945050505050565b5f600182016130ff57634e487b7160e01b5f52601160045260245ffd5b506001019056fea264697066735822122049063e1028e127daae4e01c0c9d81af006a19951b76db7b13b0ba97a3de046cd64736f6c634300081c0033"

        private var librariesLinkedBinary: String? = null

        const val FUNC_ADDCANDIDATE: String = "addCandidate"

        const val FUNC_CANDIDATEADDRESSES: String = "candidateAddresses"

        const val FUNC_CANDIDATECOUNT: String = "candidateCount"

        const val FUNC_DEACTIVATEKPUBRANCH: String = "deactivateKPUBranch"

        const val FUNC_GETALLCANDIDATES: String = "getAllCandidates"

        const val FUNC_GETALLKPUBRANCHES: String = "getAllKPUBranches"

        const val FUNC_GETALLVOTER: String = "getAllVoter"

        const val FUNC_GETBRANCHBYADDRESS: String = "getBranchByAddress"

        const val FUNC_GETCANDIDATEBYID: String = "getCandidateByID"

        const val FUNC_GETVOTERBYADDRESS: String = "getVoterByAddress"

        const val FUNC_GETVOTERBYNIK: String = "getVoterByNIK"

        const val FUNC_GETVOTERBYREGION: String = "getVoterByRegion"

        const val FUNC_KPUADMIN: String = "kpuAdmin"

        const val FUNC_KPUBRANCHADDRESSES: String = "kpuBranchAddresses"

        const val FUNC_KPUBRANCHES: String = "kpuBranches"

        const val FUNC_REGISTERKPUBRANCH: String = "registerKPUBranch"

        const val FUNC_REGISTERVOTER: String = "registerVoter"

        const val FUNC_SETKPUADMIN: String = "setKpuAdmin"

        const val FUNC_SETVOTINGSTATUS: String = "setVotingStatus"

        const val FUNC_TOGGLECANDIDATEACTIVE: String = "toggleCandidateActive"

        const val FUNC_VOTE: String = "vote"

        const val FUNC_VOTERADDRESSES: String = "voterAddresses"

        const val FUNC_VOTERNIKBYADDRESS: String = "voterNIKByAddress"

        const val FUNC_VOTERS: String = "voters"

        const val FUNC_VOTINGACTIVE: String = "votingActive"

        val CANDIDATEADDED_EVENT: Event = Event(
            "CandidateAdded",
            Arrays.asList<TypeReference<*>>(
                object : TypeReference<Utf8String?>(true) {},
                object : TypeReference<Utf8String?>() {})
        )

        val CANDIDATESTATUSCHANGED_EVENT: Event = Event(
            "CandidateStatusChanged",
            Arrays.asList<TypeReference<*>>(
                object : TypeReference<Uint256?>(true) {},
                object : TypeReference<Bool?>() {})
        )

        val KPUBRANCHDEACTIVATED_EVENT: Event = Event(
            "KPUBranchDeactivated",
            Arrays.asList<TypeReference<*>>(object : TypeReference<Address?>(true) {})
        )

        val KPUBRANCHREGISTERED_EVENT: Event = Event(
            "KPUBranchRegistered",
            Arrays.asList<TypeReference<*>>(
                object : TypeReference<Address?>(true) {},
                object : TypeReference<Utf8String?>() {},
                object : TypeReference<Utf8String?>() {})
        )

        val VOTECASTED_EVENT: Event = Event(
            "VoteCasted",
            Arrays.asList<TypeReference<*>>(
                object : TypeReference<Utf8String?>(true) {},
                object : TypeReference<Utf8String?>(true) {})
        )

        val VOTERREGISTERED_EVENT: Event = Event(
            "VoterRegistered",
            Arrays.asList<TypeReference<*>>(
                object : TypeReference<Utf8String?>(true) {},
                object : TypeReference<Address?>(true) {},
                object : TypeReference<Utf8String?>() {})
        )

        val VOTINGSTATUSCHANGED_EVENT: Event = Event(
            "VotingStatusChanged",
            Arrays.asList<TypeReference<*>>(object : TypeReference<Bool?>() {})
        )

        fun getCandidateAddedEvents(
            transactionReceipt: TransactionReceipt
        ): List<CandidateAddedEventResponse> {
            val valueList: List<EventValuesWithLog> = staticExtractEventParametersWithLog(
                CANDIDATEADDED_EVENT, transactionReceipt
            )
            val responses = ArrayList<CandidateAddedEventResponse>(valueList.size)
            for (eventValues in valueList) {
                val typedResponse = CandidateAddedEventResponse()
                typedResponse.log = eventValues.log
                typedResponse.candidateId = eventValues.indexedValues[0].value as ByteArray
                typedResponse.name = eventValues.nonIndexedValues[0].value as String
                responses.add(typedResponse)
            }
            return responses
        }

        fun getCandidateAddedEventFromLog(log: Log): CandidateAddedEventResponse {
            val eventValues = staticExtractEventParametersWithLog(CANDIDATEADDED_EVENT, log)
            val typedResponse = CandidateAddedEventResponse()
            typedResponse.log = log
            typedResponse.candidateId = eventValues.indexedValues[0].value as ByteArray
            typedResponse.name = eventValues.nonIndexedValues[0].value as String
            return typedResponse
        }

        fun getCandidateStatusChangedEvents(
            transactionReceipt: TransactionReceipt
        ): List<CandidateStatusChangedEventResponse> {
            val valueList: List<EventValuesWithLog> = staticExtractEventParametersWithLog(
                CANDIDATESTATUSCHANGED_EVENT, transactionReceipt
            )
            val responses = ArrayList<CandidateStatusChangedEventResponse>(valueList.size)
            for (eventValues in valueList) {
                val typedResponse = CandidateStatusChangedEventResponse()
                typedResponse.log = eventValues.log
                typedResponse.candidateId = eventValues.indexedValues[0].value as BigInteger
                typedResponse.isActive = eventValues.nonIndexedValues[0].value as Boolean
                responses.add(typedResponse)
            }
            return responses
        }

        fun getCandidateStatusChangedEventFromLog(
            log: Log
        ): CandidateStatusChangedEventResponse {
            val eventValues = staticExtractEventParametersWithLog(CANDIDATESTATUSCHANGED_EVENT, log)
            val typedResponse = CandidateStatusChangedEventResponse()
            typedResponse.log = log
            typedResponse.candidateId = eventValues.indexedValues[0].value as BigInteger
            typedResponse.isActive = eventValues.nonIndexedValues[0].value as Boolean
            return typedResponse
        }

        fun getKPUBranchDeactivatedEvents(
            transactionReceipt: TransactionReceipt
        ): List<KPUBranchDeactivatedEventResponse> {
            val valueList: EventValuesWithLog = staticExtractEventParametersWithLog(
                KPUBRANCHDEACTIVATED_EVENT, transactionReceipt
            )
            val responses = ArrayList<KPUBranchDeactivatedEventResponse>(valueList.size)
            for (eventValues in valueList) {
                val typedResponse = KPUBranchDeactivatedEventResponse()
                typedResponse.log = eventValues.log
                typedResponse.branchAddress = eventValues.indexedValues[0].value as String
                responses.add(typedResponse)
            }
            return responses
        }

        fun getKPUBranchDeactivatedEventFromLog(log: Log): KPUBranchDeactivatedEventResponse {
            val eventValues = staticExtractEventParametersWithLog(KPUBRANCHDEACTIVATED_EVENT, log)
            val typedResponse = KPUBranchDeactivatedEventResponse()
            typedResponse.log = log
            typedResponse.branchAddress = eventValues.indexedValues[0].value as String
            return typedResponse
        }

        fun getKPUBranchRegisteredEvents(
            transactionReceipt: TransactionReceipt
        ): List<KPUBranchRegisteredEventResponse> {
            val valueList: List<EventValuesWithLog> = staticExtractEventParametersWithLog(
                KPUBRANCHREGISTERED_EVENT, transactionReceipt
            )
            val responses = ArrayList<KPUBranchRegisteredEventResponse>(valueList.size)
            for (eventValues in valueList) {
                val typedResponse = KPUBranchRegisteredEventResponse()
                typedResponse.log = eventValues.log
                typedResponse.branchAddress = eventValues.indexedValues[0].value as String
                typedResponse.name = eventValues.nonIndexedValues[0].value as String
                typedResponse.region = eventValues.nonIndexedValues[1].value as String
                responses.add(typedResponse)
            }
            return responses
        }

        fun getKPUBranchRegisteredEventFromLog(log: Log): KPUBranchRegisteredEventResponse {
            val eventValues = staticExtractEventParametersWithLog(KPUBRANCHREGISTERED_EVENT, log)
            val typedResponse = KPUBranchRegisteredEventResponse()
            typedResponse.log = log
            typedResponse.branchAddress = eventValues.indexedValues[0].value as String
            typedResponse.name = eventValues.nonIndexedValues[0].value as String
            typedResponse.region = eventValues.nonIndexedValues[1].value as String
            return typedResponse
        }

        fun getVoteCastedEvents(
            transactionReceipt: TransactionReceipt
        ): List<VoteCastedEventResponse> {
            val valueList: List<EventValuesWithLog> = staticExtractEventParametersWithLog(
                VOTECASTED_EVENT, transactionReceipt
            )
            val responses = ArrayList<VoteCastedEventResponse>(valueList.size)
            for (eventValues in valueList) {
                val typedResponse = VoteCastedEventResponse()
                typedResponse.log = eventValues.log
                typedResponse.nik = eventValues.indexedValues[0].value as ByteArray
                typedResponse.candidateId = eventValues.indexedValues[1].value as ByteArray
                responses.add(typedResponse)
            }
            return responses
        }

        fun getVoteCastedEventFromLog(log: Log): VoteCastedEventResponse {
            val eventValues = staticExtractEventParametersWithLog(VOTECASTED_EVENT, log)
            val typedResponse = VoteCastedEventResponse()
            typedResponse.log = log
            typedResponse.nik = eventValues.indexedValues[0].value as ByteArray
            typedResponse.candidateId = eventValues.indexedValues[1].value as ByteArray
            return typedResponse
        }

        fun getVoterRegisteredEvents(
            transactionReceipt: TransactionReceipt
        ): List<VoterRegisteredEventResponse> {
            val valueList: List<EventValuesWithLog> = staticExtractEventParametersWithLog(
                VOTERREGISTERED_EVENT, transactionReceipt
            )
            val responses = ArrayList<VoterRegisteredEventResponse>(valueList.size)
            for (eventValues in valueList) {
                val typedResponse = VoterRegisteredEventResponse()
                typedResponse.log = eventValues.log
                typedResponse.nik = eventValues.indexedValues[0].value as ByteArray
                typedResponse.voterAddress = eventValues.indexedValues[1].value as String
                typedResponse.region = eventValues.nonIndexedValues[0].value as String
                responses.add(typedResponse)
            }
            return responses
        }

        fun getVoterRegisteredEventFromLog(log: Log): VoterRegisteredEventResponse {
            val eventValues = staticExtractEventParametersWithLog(VOTERREGISTERED_EVENT, log)
            val typedResponse = VoterRegisteredEventResponse()
            typedResponse.log = log
            typedResponse.nik = eventValues.indexedValues[0].value as ByteArray
            typedResponse.voterAddress = eventValues.indexedValues[1].value as String
            typedResponse.region = eventValues.nonIndexedValues[0].value as String
            return typedResponse
        }

        fun getVotingStatusChangedEvents(
            transactionReceipt: TransactionReceipt
        ): List<VotingStatusChangedEventResponse> {
            val valueList: List<EventValuesWithLog> = staticExtractEventParametersWithLog(
                VOTINGSTATUSCHANGED_EVENT, transactionReceipt
            )
            val responses = ArrayList<VotingStatusChangedEventResponse>(valueList.size)
            for (eventValues in valueList) {
                val typedResponse = VotingStatusChangedEventResponse()
                typedResponse.log = eventValues.log
                typedResponse.isActive = eventValues.nonIndexedValues[0].value as Boolean
                responses.add(typedResponse)
            }
            return responses
        }

        fun getVotingStatusChangedEventFromLog(log: Log): VotingStatusChangedEventResponse {
            val eventValues = staticExtractEventParametersWithLog(VOTINGSTATUSCHANGED_EVENT, log)
            val typedResponse = VotingStatusChangedEventResponse()
            typedResponse.log = log
            typedResponse.isActive = eventValues.nonIndexedValues[0].value as Boolean
            return typedResponse
        }

        @Deprecated("")
        fun load(
            contractAddress: String?, web3j: Web3j?, credentials: Credentials,
            gasPrice: BigInteger?, gasLimit: BigInteger?
        ): Votechain {
            return Votechain(contractAddress, web3j, credentials, gasPrice, gasLimit)
        }

        @Deprecated("")
        fun load(
            contractAddress: String?, web3j: Web3j?,
            transactionManager: TransactionManager?, gasPrice: BigInteger?, gasLimit: BigInteger?
        ): Votechain {
            return Votechain(contractAddress, web3j, transactionManager, gasPrice, gasLimit)
        }

        fun load(
            contractAddress: String?, web3j: Web3j?, credentials: Credentials,
            contractGasProvider: ContractGasProvider?
        ): Votechain {
            return Votechain(contractAddress, web3j, credentials, contractGasProvider)
        }

        fun load(
            contractAddress: String?, web3j: Web3j?,
            transactionManager: TransactionManager?, contractGasProvider: ContractGasProvider?
        ): Votechain {
            return Votechain(contractAddress, web3j, transactionManager, contractGasProvider)
        }

        fun deploy(
            web3j: Web3j?, credentials: Credentials?,
            contractGasProvider: ContractGasProvider?
        ): RemoteCall<Votechain> {
            return deployRemoteCall(
                Votechain::class.java, web3j, credentials, contractGasProvider,
                deploymentBinary, ""
            )
        }

        fun deploy(
            web3j: Web3j?, transactionManager: TransactionManager?,
            contractGasProvider: ContractGasProvider?
        ): RemoteCall<Votechain> {
            return deployRemoteCall(
                Votechain::class.java, web3j, transactionManager, contractGasProvider,
                deploymentBinary, ""
            )
        }

        @Deprecated("")
        fun deploy(
            web3j: Web3j?, credentials: Credentials?,
            gasPrice: BigInteger?, gasLimit: BigInteger?
        ): RemoteCall<Votechain> {
            return deployRemoteCall(
                Votechain::class.java, web3j, credentials, gasPrice, gasLimit,
                deploymentBinary, ""
            )
        }

        @Deprecated("")
        fun deploy(
            web3j: Web3j?, transactionManager: TransactionManager?,
            gasPrice: BigInteger?, gasLimit: BigInteger?
        ): RemoteCall<Votechain> {
            return deployRemoteCall(
                Votechain::class.java, web3j, transactionManager, gasPrice, gasLimit,
                deploymentBinary, ""
            )
        }

        fun linkLibraries(references: List<Contract.LinkReference?>?) {
            librariesLinkedBinary = linkBinaryWithReferences(BINARY, references)
        }

        private val deploymentBinary: String
            get() = if (librariesLinkedBinary != null) {
                librariesLinkedBinary!!
            } else {
                BINARY
            }
    }
}
