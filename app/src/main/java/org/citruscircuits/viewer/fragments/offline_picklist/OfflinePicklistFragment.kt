//package org.citruscircuits.viewer.fragments.offline_picklist
//
//import android.annotation.SuppressLint
//import android.app.Activity
//import android.app.AlertDialog
//import android.app.Dialog
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.view.inputmethod.InputMethodManager
//import androidx.fragment.app.DialogFragment
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.ItemTouchHelper
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import kotlinx.coroutines.runBlocking
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.decodeFromString
//import kotlinx.serialization.encodeToString
//import kotlinx.serialization.json.Json
//import org.citruscircuits.viewer.MainViewerActivity
//import org.citruscircuits.viewer.R
//import org.citruscircuits.viewer.constants.Constants
//import org.citruscircuits.viewer.data.PicklistApi
//import org.citruscircuits.viewer.databinding.ExportPicklistPopupBinding
//import org.citruscircuits.viewer.databinding.FragmentOfflinePicklistBinding
//import org.citruscircuits.viewer.databinding.ImportPicklistPopupBinding
//import org.citruscircuits.viewer.fragments.live_picklist.LivePicklistFragment
//import org.citruscircuits.viewer.showError
//import java.io.File
//
//class OfflinePicklistFragment : Fragment() {
//    var picklistData = PicklistData()
//    var importedPicklistData = PicklistData()
//
//    private lateinit var adapter: OfflinePicklistAdapter
//    private lateinit var binding: FragmentOfflinePicklistBinding
//    private val dataFile = File(Constants.DOWNLOADS_FOLDER, "picklist.json")
//    private val dataFileImported = File(Constants.DOWNLOADS_FOLDER, "picklist-imported.json")
//
//    private fun switchScreen() {
//        val livePicklistFragment = LivePicklistFragment()
//        val ft = parentFragmentManager.beginTransaction()
//        if (parentFragmentManager.fragments.last().tag != "livepicklistFragment") {
//            ft.addToBackStack(null)
//        }
//        ft.replace(R.id.nav_host_fragment, livePicklistFragment, "livepicklistFragment").commit()
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View {
//        if (!dataFile.exists()) dataFile.writeText(Json.encodeToString(PicklistData()))
//        if (!dataFileImported.exists()) dataFileImported.writeText(Json.encodeToString(PicklistData()))
//        binding = FragmentOfflinePicklistBinding.inflate(inflater, container, false)
//        // Init adapter
//        itemTouchHelper.attachToRecyclerView(binding.rvOfflinePicklist)
//        adapter = OfflinePicklistAdapter(this)
//        binding.rvOfflinePicklist.layoutManager = LinearLayoutManager(context)
//        binding.rvOfflinePicklist.adapter = adapter
//        binding.btnSwitchOnline.setOnClickListener { switchScreen() }
//        binding.btnSwitchOffline.setOnClickListener { switchScreen() }
//        updateData()
//        // Init import button
//        binding.btnDownload.setOnClickListener {
//            ImportPopup {
//                when (it) {
//                    ImportType.TeamList -> {
//                        Log.d("offline_picklist", "Importing team list: ${MainViewerActivity.teamList}")
//                        saveData(MainViewerActivity.teamList, emptyList())
//                    }
//
//                    ImportType.Server -> runBlocking {
//                        try {
//                            val data = PicklistApi.getPicklist(Constants.EVENT_KEY)
//                            saveData(data.ranking, data.dnp)
//                        } catch (e: Throwable) {
//                            Log.e("offline_picklist", "Error importing picklist from server", e)
//                            showError(requireContext(), "Error pulling picklist")
//                        }
//                    }
//                }
//                importedPicklistData = picklistData
//                dataFileImported.writeText(Json.encodeToString(importedPicklistData))
//                updateData()
//            }.show(parentFragmentManager, "import_popup")
//        }
//        //Temporarily removing picklist upload button
//        // Init export button
////        binding.btnUpload.setOnClickListener {
////            ExportPopup {
////                runBlocking {
////                    getData()
////                    try {
////                        when (val resp =
////                            PicklistApi.setPicklist(picklistData, it, Constants.EVENT_KEY)) {
////                            is PicklistApi.PicklistSetResponse.Success -> {
////                                showSuccess(
////                                    requireContext(),
////                                    "Picklist uploaded. Deleted ${resp.deleted} old teams"
////                                )
////                            }
////
////                            is PicklistApi.PicklistSetResponse.Error -> {
////                                showError(
////                                    requireContext(), "Error uploading picklist: ${resp.error}"
////                                )
////                            }
////                        }
////                    } catch (e: Throwable) {
////                        Log.e("offline_picklist", "Error exporting picklist to server", e)
////                        showError(requireContext(), "Error pushing picklist")
////                    }
////                }
////            }.show(parentFragmentManager, "export_popup")
////        }
//
////        // Init refresh button
////        binding.btnPicklistRefresh.setOnClickListener {
////            picklistData = getData()
////            updateData()
////        }
//
//        // Populate initial data
//        getData()
//        updateData()
//
//        return binding.root
//    }
//
//    private fun getData() {
//        picklistData = Json.decodeFromString(dataFile.readText())
//        importedPicklistData = Json.decodeFromString(dataFileImported.readText())
//    }
//
//    fun saveData(ranking: List<String>, dnp: List<String>) {
//        val data = PicklistData(ranking, dnp)
//        dataFile.writeText(Json.encodeToString(data))
//        picklistData = PicklistData(ranking, dnp)
//        Log.d("offline_picklist", "Saved data: $data")
//    }
//
//    @SuppressLint("NotifyDataSetChanged")
//    fun updateData() {
//        adapter.submitList(picklistData.ranking + picklistData.dnp)
//        adapter.notifyDataSetChanged()
//    }
//
////    fun handleRequest(req: OfflinePicklistRequest) {
////        when (req) {
////            is OfflinePicklistRequest.UpdateData -> {
////                val currentData = getData()
////                val newRanking = currentData.ranking.toMutableList()
////                newRanking.add(req.to_place - 1, newRanking.removeAt(req.from_place - 1))
////                saveData(newRanking, currentData.dnp)
////            }
////            is OfflinePicklistRequest.ToggleDNP -> {
////                val currentData = getData()
////                val newRanking = currentData.ranking.toMutableList()
////                val newDnp = currentData.dnp.toMutableList()
////                val teamNumber = req.team_number.toString()
////                if (newDnp.contains(teamNumber)) {
////                    newDnp.remove(teamNumber)
////                    newRanking.add(teamNumber)
////                } else {
////                    newDnp.add(teamNumber)
////                    newRanking.remove(teamNumber)
////                }
////            }
////        }
////        adapter.notifyDataSetChanged()
////    }
//
//    private val itemTouchHelper by lazy {
//        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(
//            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
//            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
//        ) {
//            var lastMove: LastMove? = null
//            override fun onMove(
//                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
//            ): Boolean {
//                val from = viewHolder.adapterPosition
//                val to = target.adapterPosition
//                Log.d("picklist", "Move event: from: $from to: $to")
//                lastMove?.to = to
//                if ((lastMove != null) and !lastMove!!.fromSet) {
//                    lastMove?.fromSet = true
//                    lastMove?.from = from
//                }
//                Log.d("picklist", "LastMove: $lastMove")
//                adapter.notifyItemMoved(from, to)
//                return true
//            }
//
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                Log.d("offline_picklist", "Swiped direction: $direction")
//                adapter.handleDNPToggle(viewHolder.adapterPosition)
//            }
//
//            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
//                super.onSelectedChanged(viewHolder, actionState)
//                viewHolder as OfflinePicklistAdapter.OfflinePicklistViewHolder?
//                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
//                    Log.d("offline_picklist", "started drag")
//                    lastMove = LastMove(0, 0)
//                    viewHolder?.itemView?.alpha = 0.5f
//                }
//                if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
//                    Log.d("offline_picklist", "Done dragging")
//                    lastMove?.let {
//                        adapter.handleOrderChange(it.from, it.to)
//                        lastMove = null
//                    }
//                }
//            }
//
//            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
//                super.clearView(recyclerView, viewHolder)
//                viewHolder.itemView.alpha = 1.0f
//            }
//        }
//        ItemTouchHelper(simpleItemTouchCallback)
//    }
//}
//
//@Serializable
//data class PicklistData(val ranking: List<String> = emptyList(), val dnp: List<String> = emptyList())
//
//data class LastMove(var from: Int, var to: Int, var fromSet: Boolean = false)
//
//enum class ImportType { TeamList, Server }
//
//class ImportPopup(val onImport: (type: ImportType) -> Unit) : DialogFragment() {
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return activity?.let {
//            val builder = AlertDialog.Builder(it)
//            val inflater = requireActivity().layoutInflater
//            val view = ImportPicklistPopupBinding.inflate(inflater, null, false)
//            view.btnTeamList.setOnClickListener {
//                onImport(ImportType.TeamList)
//                dismiss()
//            }
//            view.btnServer.setOnClickListener {
//                onImport(ImportType.Server)
//                dismiss()
//            }
//            builder.setView(view.root).setNegativeButton("Close") { dialog, _ -> dialog.cancel() }
//            builder.setTitle("Import")
//            builder.create()
//        } ?: throw IllegalStateException("Activity cannot be null")
//    }
//}
//
//class ExportPopup(val onExport: (password: String) -> Unit) : DialogFragment() {
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = activity?.let {
//        val builder = AlertDialog.Builder(it)
//        val inflater = requireActivity().layoutInflater
//        val view = ExportPicklistPopupBinding.inflate(inflater, null, false)
//        view.btnServer.setOnClickListener { btnView ->
//            onExport(view.etPassword.text.toString().trim())
//            (it.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(
//                btnView.windowToken, 0
//            )
//            dismiss()
//        }
//        builder.setView(view.root).setNegativeButton("Close") { dialog, _ -> dialog.cancel() }
//        builder.setTitle("Export")
//        builder.create()
//    } ?: throw IllegalStateException("Activity cannot be null")
//}
